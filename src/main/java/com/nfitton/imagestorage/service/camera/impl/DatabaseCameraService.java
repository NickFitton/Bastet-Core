package com.nfitton.imagestorage.service.camera.impl;

import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.repository.CameraRepository;
import com.nfitton.imagestorage.service.camera.CameraService;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DatabaseCameraService implements CameraService {

  private CameraRepository repository;

  @Autowired public DatabaseCameraService(CameraRepository repository) {
    this.repository = repository;
  }

  @Override public Mono<Camera> register(Camera camera) {
    ZonedDateTime now = ZonedDateTime.now();

    Camera cameraDetails = Camera.Builder
        .newBuilder()
        .withName(camera.getName())
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .build();

    return this.save(cameraDetails);
  }

  @Override public Mono<String> attachKey(UUID cameraId, String devicePublicKey) {
    return null;
  }

  @Override public Mono<String> validate(String encryptedId) {
    return null;
  }

  @Override public Flux<Camera> getAll() {
    return findAll();
  }

  private Mono<Camera> save(Camera camera) {
    return Mono.fromCallable(() -> repository.save(camera)).subscribeOn(Schedulers.elastic());
  }

  private Flux<Camera> findAll() {
    return Flux.fromIterable(repository.findAll());
  }
}
