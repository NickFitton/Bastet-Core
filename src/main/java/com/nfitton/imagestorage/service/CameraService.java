package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Camera;

import java.util.Optional;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CameraService {

  /**
   * Registers a new camera to the system
   *
   * @param camera the camera details
   * @return the saved Camera
   */
  Mono<Camera> save(Camera camera);

  /**
   * Checks that the given camera ID exists and that the encrypted passwords match.
   * @param cameraId the ID to authenticate by
   * @param encryptedPassword the encryted password attempt
   * @return the UUID of the camera if the authentication passes, else throws
   */
  Mono<UUID> authenticate(UUID cameraId, String encryptedPassword);

  /**
   * Receives a UUID and checks that a camera exists by the given UUID.
   * @param cameraId the {@link UUID} to check by
   * @return true if camera exists, else false. Returns in a reactive {@link Mono}
   */
  Mono<Boolean> cameraExists(UUID cameraId);

  Mono<Camera> imageTaken(UUID cameraId);

  Mono<Optional<Camera>> findById(UUID cameraId);

  /**
   * Get a list of all cameras stored in the system
   *
   * @return a stream of {@link Camera} objects stored
   */
  Flux<UUID> getAll();

  Mono<Boolean> deleteCamera(UUID cameraId);
}
