package com.nfitton.imagestorage.util;

import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.CameraV1;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class CameraUtil {

  public static ClientResponse createCamera(WebClient client, CameraV1 camera) {
    return client.post()
        .uri("/v1/cameras")
        .syncBody(camera)
        .exchange()
        .block();
  }

  public static ClientResponse getCameras(WebClient client, String sessionToken) {
    return client.get()
        .uri("/v1/cameras")
        .exchange()
        .block();
  }

  public static ClientResponse getCamera(WebClient client, String sessionToken, UUID cameraId) {
    return client.get()
        .uri("/v1/cameras/" + cameraId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse deleteCamera(WebClient client, String sessionToken, UUID cameraId) {
    return client.delete()
        .uri("/v1/cameras/" + cameraId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

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
