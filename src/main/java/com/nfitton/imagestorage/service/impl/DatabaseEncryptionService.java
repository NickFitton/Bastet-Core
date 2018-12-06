package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.DHKey;
import com.nfitton.imagestorage.entity.KeyType;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.repository.CameraKeyRepository;
import com.nfitton.imagestorage.service.EncryptionService;
import com.nfitton.imagestorage.utility.KeyUtils;

import static com.nfitton.imagestorage.utility.KeyUtils.fromMinHexString;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DatabaseEncryptionService implements EncryptionService {

  private CameraKeyRepository keyRepository;

  @Autowired public DatabaseEncryptionService(CameraKeyRepository keyRepository) {
    this.keyRepository = keyRepository;
  }

  @Override public <T> Mono<T> decryptObject(
      UUID cameraId, String encryptedObject, String parameters, Class<T> objectClass) {
    return Mono
        .fromCallable(() -> keyRepository.findByCameraIdAndType(cameraId, KeyType.SECRET))
        .flatMap(optionalKey -> {
          if (optionalKey.isPresent()) {
            return Mono.just(optionalKey.get());
          } else {
            return Mono.error(new NotFoundException(
                "Camera has not handshaked with backend service yet, backend secret not found"));
          }
        })
        .map(DHKey::getPublicKey)
        .map(KeyUtils::fromMinHexString)
        .map(secret -> new SecretKeySpec(secret, 0, 16, "AES"))
        .flatMap(keySpec -> {
          try {
            AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
            aesParams.init(fromMinHexString(parameters));
            Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aliceCipher.init(Cipher.DECRYPT_MODE, keySpec, aesParams);
            byte[] recovered = aliceCipher.doFinal(fromMinHexString(encryptedObject));
            return Mono.just(new String(recovered));
          } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            return Mono.error(new InternalServerException(e));
          } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | IOException e) {
            return Mono.error(new BadRequestException("You broke it"));
          }
        })
        .flatMap(decryptedJson -> {
          ObjectMapper mapper = new ObjectMapper()
              .registerModule(new ParameterNamesModule())
              .registerModule(new Jdk8Module())
              .registerModule(new JavaTimeModule());
          return Mono.fromCallable(() -> mapper.readValue(decryptedJson, objectClass));
        });
  }

  @Override public <T> Mono<String> encryptObject(UUID cameraId, T object, Class<T> objectClass) {
    return null;
  }
}
