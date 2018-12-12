package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.repository.CameraRepository;
import com.nfitton.imagestorage.service.CameraService;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DatabaseCameraService implements CameraService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCameraService.class);

  private final PasswordEncoder encoder;
  private CameraRepository cameraRepository;

  @Autowired
  public DatabaseCameraService(
      CameraRepository cameraRepository, PasswordEncoder encoder) {
    this.cameraRepository = cameraRepository;
    this.encoder = encoder;
  }

  @Override
  public Mono<Camera> save(Camera camera) {
    ZonedDateTime now = ZonedDateTime.now();

    Camera cameraDetails = Camera.Builder
        .newBuilder()
        .withPassword(camera.getPassword())
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .build();

    return this.saveCamera(cameraDetails);
  }

  @Override
  public Mono<UUID> authenticate(UUID cameraId, String password) {
    return Mono.fromCallable(() -> cameraRepository.findById(cameraId))
        .flatMap(camera -> {
          if (!camera.isPresent()) {
            return Mono.error(new BadRequestException("Invalid username/password combination"));
          }
          boolean matches = encoder.matches(password, camera.get().getPassword());
          if (matches) {
            return Mono.just(camera.get().getId());
          } else {
            return Mono.error(new BadRequestException("Invalid username/password combination"));
          }
        });
  }

  @Override
  public Mono<Boolean> cameraExists(UUID cameraId) {
    return Mono.fromCallable(() -> cameraRepository.existsById(cameraId));
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
          camera.imageTaken();

          return saveCamera(camera);
        });
  }

  @Override
  public Flux<UUID> getAll() {
    return Flux.fromIterable(cameraRepository.findAllCamera());
  }

  @Override
  public Mono<Boolean> deleteCamera(UUID cameraId) {
    return Mono.fromCallable(() -> {
      cameraRepository.deleteById(cameraId);
      return true;
    });
  }

  @Override
  public Mono<Optional<Camera>> findById(UUID id) {
    return Mono.fromCallable(() -> cameraRepository.findById(id));
  }

  private Mono<Camera> saveCamera(Camera camera) {
    return Mono.fromCallable(() -> cameraRepository.save(camera)).subscribeOn(Schedulers.elastic());
  }

  private boolean existsById(UUID cameraId) {
    return cameraRepository.existsById(cameraId);
  }

  private Flux<Camera> findAll() {
    return Flux.fromIterable(cameraRepository.findAll());
  }
}
