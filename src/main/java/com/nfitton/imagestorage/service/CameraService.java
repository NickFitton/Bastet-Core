package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Camera;
import java.util.Optional;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface CameraService extends AccountService<Camera, UUID> {

  Mono<Camera> imageTaken(UUID cameraId);

  Mono<Optional<Camera>> findById(UUID cameraId);

  Mono<Camera> claimCamera(UUID cameraId, UUID userId);
}
