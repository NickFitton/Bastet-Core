package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.entity.DHKey;
import com.nfitton.imagestorage.entity.KeyType;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.repository.CameraKeyRepository;
import com.nfitton.imagestorage.repository.CameraRepository;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.utility.KeyUtils;

import static com.nfitton.imagestorage.utility.KeyUtils.*;

import javax.crypto.KeyAgreement;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DatabaseCameraService implements CameraService {

  private CameraRepository cameraRepository;
  private CameraKeyRepository keyRepository;

  @Autowired public DatabaseCameraService(
      CameraRepository cameraRepository, CameraKeyRepository keyRepository) {
    this.cameraRepository = cameraRepository;
    this.keyRepository = keyRepository;
  }

  @Override public Mono<Camera> register(Camera camera) {
    ZonedDateTime now = ZonedDateTime.now();

    Camera cameraDetails = Camera.Builder
        .newBuilder()
        .withName(camera.getName())
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .build();

    return this.saveCamera(cameraDetails);
  }

  @Override public Mono<String> startHandshake(UUID cameraId, String devicePublicKey) {
    if (!existsById(cameraId)) {
      return Mono.error(new NotFoundException(String.format("Camera by id %s does not exist",
                                                            cameraId)));
    }

    DHKey deviceDHKey = DHKey.Builder
        .newBuilder()
        .withPublic(devicePublicKey)
        .withCameraId(cameraId)
        .withKeyType(KeyType.DEVICE)
        .build();

    return saveKey(deviceDHKey).flatMap(savedKey -> {
      byte[] savedDevicePublicKey = fromMinHexString(savedKey.getPublicKey());
      try {
        DHPublicKey dhPublicKey = parseEncodedKey(savedDevicePublicKey);
        KeyPair keyPair = keyPairFromSpec(dhPublicKey.getParams());
        KeyAgreement keyAgreement = createKeyAgreement(keyPair);
        keyAgreement.doPhase(dhPublicKey, true);
        byte[] secret = new byte[256];
        keyAgreement.generateSecret(secret, 0);

        DHKey backendKey = DHKey.Builder
            .newBuilder()
            .withPublic(KeyUtils.toMinHexString(secret))
            .withKeyType(KeyType.SECRET)
            .withCameraId(cameraId)
            .build();
        return saveKey(backendKey).then(Mono.just(KeyUtils.toMinHexString(keyPair.getPublic().getEncoded())));
      } catch (NoSuchAlgorithmException | InvalidKeyException | ShortBufferException e) {
        return Mono.error(new InternalServerException(e));
      } catch (InvalidKeySpecException | InvalidAlgorithmParameterException e) {
        return Mono.error(new BadRequestException(
            "Given key did not hold a valid key specification."));
      }
    });
  }

  @Override public Mono<String> validate(String encryptedId) {
    return null;
  }

  @Override public Flux<Camera> getAll() {
    return findAll();
  }

  private Mono<Camera> saveCamera(Camera camera) {
    return Mono.fromCallable(() -> cameraRepository.save(camera)).subscribeOn(Schedulers.elastic());
  }

  private Mono<DHKey> saveKey(DHKey key) {
    return Mono.fromCallable(() -> keyRepository.save(key));
  }

  private boolean existsById(UUID cameraId) {
    return cameraRepository.existsById(cameraId);
  }

  private Flux<Camera> findAll() {
    return Flux.fromIterable(cameraRepository.findAll());
  }
}
