package com.nfitton.imagestorage.service.camera;

import com.nfitton.imagestorage.entity.Camera;

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
  Mono<Camera> register(Camera camera);

  /**
   * Receive the cameras public key and the camera id, saves the key to a file and uses the key to
   * encrypt the backend public key for the camera to decrypt.
   *
   * @param devicePublicKey the public key of the device
   * @return the public key of the backend encoded with the devices public key
   */
  Mono<String> attachKey(UUID cameraId, String devicePublicKey);

  /**
   * Receives the cameras id encrypted with the backend public key, if the backend can decrypt and
   * read the camera id successfully then it returns an access token.
   *
   * @param encryptedId the cameras {@link UUID} id encrypted with the backends public key
   * @return either an empty mono (camera wasn't valid) or an access token
   */
  Mono<String> validate(String encryptedId);

  /**
   * Get a list of all cameras stored in the system
   *
   * @return a stream of {@link Camera} objects stored
   */
  Flux<Camera> getAll();
}
