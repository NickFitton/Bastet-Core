package com.nfitton.imagestorage.service.impl;

import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.repository.CameraRepository;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DatabaseCameraService implements CameraService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCameraService.class);

  private final AuthenticationService authenticationService;
  private CameraRepository cameraRepository;

  @Autowired
  public DatabaseCameraService(
      CameraRepository cameraRepository, AuthenticationService authenticationService) {
    this.cameraRepository = cameraRepository;
    this.authenticationService = authenticationService;
  }

  @Override
  public Mono<Camera> save(Camera camera) {
    ZonedDateTime now = ZonedDateTime.now();

    Camera cameraDetails = Camera.Builder
        .newBuilder()
        .withPassword(camera.getPassword())
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .withLastActive(now)
        .build();

    return this.saveCamera(cameraDetails);
  }

  @Override
  public Mono<UUID> authenticate(String id, String password, AccountType type) {
    switch (type) {
      case CAMERA:
        return authenticationService
            .authenticate(UUID.fromString(id), password, CAMERA, cameraRepository);
      default:
        throw new InternalServerException(
            String.format("User service can't authenticate given type: %s", type));
    }
  }

  @Override
  public Mono<Camera> imageTaken(UUID cameraId) {
    return findById(cameraId)
        .map(camera -> {
          if (!camera.isPresent()) {
            LOGGER.error("Image data saved when related camera id does not exist");
            return null;
          } else {
            return camera.get();
          }
        })
        .filter(Objects::nonNull)
        .flatMap(camera -> {
          camera.isActive();

          return saveCamera(camera);
        });
  }

  @Override
  public Mono<Boolean> deleteById(UUID cameraId) {
    return Mono.fromCallable(() -> {
      cameraRepository.deleteById(cameraId);
      return true;
    });
  }

  @Override
  public Mono<Optional<Camera>> findById(UUID id) {
    return Mono.fromCallable(() -> cameraRepository.findById(id));
  }

  @Override
  public Flux<UUID> getAllIds() {
    return Flux.fromIterable(cameraRepository.findAllCamera());
  }

  @Override
  public Mono<Boolean> existsById(UUID id) {
    return Mono.fromCallable(() -> cameraRepository.existsById(id));
  }

  private Mono<Camera> saveCamera(Camera camera) {
    return Mono.fromCallable(() -> cameraRepository.save(camera)).subscribeOn(Schedulers.elastic());
  }
}
