package com.nfitton.imagestorage.util;

import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.CameraV1;
import java.util.UUID;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class CameraUtil {

  /**
   * Returns a request to create a camera given the client and camera to save.
   *
   * @param client the client to make the request on
   * @param camera the camera to save
   * @return the response from the server
   */
  public static ClientResponse createCamera(WebClient client, CameraV1 camera) {
    return client.post()
        .uri("/v1/cameras")
        .syncBody(camera)
        .exchange()
        .block();
  }

  /**
   * Returns a request to get the camera data related to the given cameraId.
   *
   * @param client the client to make the request on
   * @param sessionToken the session token of the requesting user
   * @param cameraId the camera id to get data by
   * @return the response from the server
   */
  public static ClientResponse getCamera(WebClient client, String sessionToken, UUID cameraId) {
    return client.get()
        .uri("/v1/cameras/" + cameraId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  /**
   * Returns a request to delete the camera data related to the given cameraId.
   *
   * @param client the client to make the request on
   * @param sessionToken the session token of the requesting user
   * @param cameraId the camera id to delete by
   * @return the response from the server
   */
  public static ClientResponse deleteCamera(WebClient client, String sessionToken, UUID cameraId) {
    return client.delete()
        .uri("/v1/cameras/" + cameraId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  /**
   * Returns a new {@link CameraV1} to save using just the given password.
   *
   * @param password the password for the new camera
   * @return the build {@link CameraV1} to save
   */
  public static CameraV1 passwordOnly(String password) {
    return new CameraV1(
        null,
        null,
        null,
        password,
        null,
        null,
        null);
  }
}
