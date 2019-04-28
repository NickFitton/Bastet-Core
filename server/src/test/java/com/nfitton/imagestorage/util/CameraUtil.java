package com.nfitton.imagestorage.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import java.io.File;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class CameraUtil {

  public static UUID createCamera(WebClient client, CameraV1 camera, ObjectMapper mapper) {
    return createCamera(client, camera)
        .bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(CameraV1.class, mapper)
        .getId();
  }

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

  public static ClientResponse getCameras(WebClient client, String sessionToken) {
    return client.get()
        .uri("/v1/cameras/")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
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
  public static ClientResponse claimCamera(
      WebClient client, String sessionToken, UUID cameraId, String cameraName) {
    return client.patch()
        .uri("/v1/cameras/" + cameraId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .syncBody(nameOnly(cameraName))
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

  public static CameraV1 nameOnly(String name) {
    return new CameraV1(
        null,
        null,
        name,
        null,
        null,
        null,
        null);
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

  public static ImageMetadataV1 postMotionData(
      WebClient client, String sessionToken, ImageMetadataV1 motionData, ObjectMapper mapper) {
    return postMotionData(client, sessionToken, motionData).bodyToMono(OutgoingDataV1.class).block()
        .parseData(ImageMetadataV1.class, mapper);
  }

  public static ClientResponse postMotionData(
      WebClient client, String sessionToken, ImageMetadataV1 motionData) {
    return client.post()
        .uri("/v1/motion")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .syncBody(motionData)
        .exchange()
        .block();
  }

  public static ClientResponse patchMotionData(
      WebClient client,
      String sessionToken,
      UUID metadataId,
      File image) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(image));
    return client.patch()
        .uri("/v1/motion/" + metadataId)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .syncBody(body)
        .exchange()
        .block();
  }

  public static ClientResponse listMotionData(
      WebClient client, String sessionToken, String... cameraIds) {
    return client.get()
        .uri("/v1/motion?cameras=" + String.join(",", cameraIds))
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse getMotionData(
      WebClient client, String sessionToken, UUID motionId) {
    return client.get()
        .uri("/v1/motion/" + motionId)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse getMotionImageData(
      WebClient client, String sessionToken, UUID motionId) {
    return client.get()
        .uri("/v1/motion/" + motionId + "/image")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .accept(MediaType.IMAGE_JPEG)
        .exchange()
        .block();
  }
}
