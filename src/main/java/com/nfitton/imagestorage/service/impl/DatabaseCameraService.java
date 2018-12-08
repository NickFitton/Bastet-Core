package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.repository.CameraRepository;
import com.nfitton.imagestorage.service.CameraService;

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

  @Autowired public DatabaseCameraService(
      CameraRepository cameraRepository) {
    this.cameraRepository = cameraRepository;
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

  @Override public Mono<String> validate(String encryptedId) {
    return null;
  }

  @Override public Flux<Camera> getAll() {
    return findAll();
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
