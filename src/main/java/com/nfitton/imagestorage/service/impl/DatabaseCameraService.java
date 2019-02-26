package com.nfitton.imagestorage.service.impl;

import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.exception.ForbiddenException;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.exception.NotFoundException;
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
  private CameraRepository repository;

  @Autowired
  public DatabaseCameraService(
      CameraRepository repository, AuthenticationService authenticationService) {
    this.repository = repository;
    this.authenticationService = authenticationService;
  }

  private static NotFoundException cameraNotFound() {
    return new NotFoundException("Camera not found by given id");
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
    if (type == CAMERA) {
      return authenticationService
          .authenticate(UUID.fromString(id), password, CAMERA, repository);
    }
    throw new InternalServerException(
        String.format("User service can't authenticate given type: %s", type));
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
      repository.deleteById(cameraId);
      return true;
    }).subscribeOn(Schedulers.elastic());
  }

  @Override
  public Mono<Optional<Camera>> findById(UUID id) {
    return Mono.fromCallable(() -> repository.findById(id)).subscribeOn(Schedulers.elastic());
  }

  @Override
  public Mono<Camera> updateCamera(UUID cameraId, UUID userId, Camera updatedInfo) {
    return findById(cameraId)
        .map(optionalCamera -> optionalCamera.orElseThrow(DatabaseCameraService::cameraNotFound))
        .map(existingCamera -> {
          Camera.Builder builder = Camera.Builder.clone(existingCamera)
              .withUpdatedAt(ZonedDateTime.now());
          if (existingCamera.getOwnerId() == null) {
            builder.withOwnerId(userId);
          } else if (!existingCamera.getOwnerId().toString().equals(userId.toString())) {
            LOGGER
                .warn("{} tried to update camera owned by {}", userId, existingCamera.getOwnerId());
            throw new ForbiddenException("User does not own camera");
          }
          if (updatedInfo.getName() != null) {
            builder.withName(updatedInfo.getName());
          }
          return builder.build();
        })
        .flatMap(this::saveCamera);
  }

  @Override
  public Flux<Camera> findAllOwnedById(UUID ownerId) {
    return Flux.fromIterable(repository.findAllByOwnerId(ownerId))
        .subscribeOn(Schedulers.elastic());
  }

  @Override
  public Flux<UUID> getAllIds() {
    return Flux.fromIterable(repository.findAllCamera()).subscribeOn(Schedulers.elastic());
  }

  @Override
  public Mono<Boolean> existsById(UUID id) {
    return Mono.fromCallable(() -> repository.existsById(id))
        .subscribeOn(Schedulers.elastic());
  }

  private Mono<Camera> saveCamera(Camera camera) {
    return Mono.fromCallable(() -> repository.save(camera)).subscribeOn(Schedulers.elastic());
  }
}
