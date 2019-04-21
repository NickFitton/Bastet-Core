package com.nfitton.imagestorage.service;

import static com.nfitton.imagestorage.util.CameraUtil.claimCamera;
import static com.nfitton.imagestorage.util.CameraUtil.getMotionData;
import static com.nfitton.imagestorage.util.CameraUtil.getMotionImageData;
import static com.nfitton.imagestorage.util.CameraUtil.listMotionData;
import static com.nfitton.imagestorage.util.CameraUtil.patchMotionData;
import static com.nfitton.imagestorage.util.CameraUtil.postMotionData;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.util.CameraUtil;
import java.io.File;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

class MotionHandlerIT extends BaseClientIT {

  @Test
  void savingMetadataIsSuccessful() {
    // GIVEN a camera is registered
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);
    UUID cameraId = CameraUtil.createCamera(client, camera, objectMapper);
    String sessionToken = getCameraToken(cameraId, userPassword, objectMapper);

    // WHEN a camera posts metadata
    ImageMetadataV1 newMetadata = new ImageMetadataV1(
        null, null, ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(),
        null, null, false, null);
    ClientResponse response = postMotionData(client, sessionToken, newMetadata);

    // THEN the request is successful
    assertEquals(HttpStatus.CREATED, response.statusCode());
  }

  @Test
  void updatingMetadataWithImageEntityIsSuccessful() {
    // GIVEN a camera is registered
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);
    UUID cameraId = CameraUtil.createCamera(client, camera, objectMapper);
    String sessionToken = getCameraToken(cameraId, userPassword, objectMapper);
    ImageMetadataV1 newMetadata = new ImageMetadataV1(
        null, null, ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(),
        null, null, false, null);
    ImageMetadataV1 receivedMetadata = postMotionData(client, sessionToken, newMetadata)
        .bodyToMono(OutgoingDataV1.class).block().parseData(ImageMetadataV1.class, objectMapper);

    // WHEN a camera posts metadata
    URL url = Thread.currentThread().getContextClassLoader().getResource("motion/imageA.jpeg");
    File file = new File(url.getPath());
    ClientResponse response = patchMotionData(client, sessionToken, receivedMetadata.getId(), file);

    // THEN upload is successful
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());
  }

  @Test
  void userWithAccessToCameraCanListMotion() {
    // GIVEN a camera is claimed and has posted motion data
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);
    UUID cameraId = CameraUtil.createCamera(client, camera, objectMapper);
    String userSessionToken = getSessionToken();
    claimCamera(client, userSessionToken, cameraId, "Camera A");
    String sessionToken = getCameraToken(cameraId, userPassword, objectMapper);
    ImageMetadataV1 newMetadata = new ImageMetadataV1(
        null, null, ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(),
        null, null, false, null);
    ImageMetadataV1 updatedMetadata = postMotionData(
        client, sessionToken, newMetadata, objectMapper);

    // WHEN a user that can access the camera requests motion data
    ClientResponse response = listMotionData(client, userSessionToken, cameraId.toString());

    // THEN the request is successful
    assertEquals(HttpStatus.OK, response.statusCode());
    // AND the uploaded metadata is listed
    List<ImageMetadataV1> returnedMetadata = response.bodyToMono(OutgoingDataV1.class).block()
        .parseData(new TypeReference<List<ImageMetadataV1>>() {
        }, objectMapper);
    assertEquals(1, returnedMetadata.size());
  }

  @Test
  void userWithAccessToCameraCanReferenceMotionById() {
    // GIVEN a camera is claimed and has posted motion data
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);
    UUID cameraId = CameraUtil.createCamera(client, camera, objectMapper);
    String userSessionToken = getSessionToken();
    claimCamera(client, userSessionToken, cameraId, "Camera A");
    String sessionToken = getCameraToken(cameraId, userPassword, objectMapper);
    ImageMetadataV1 newMetadata = new ImageMetadataV1(
        null, null, ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(),
        null, null, false, null);
    ImageMetadataV1 updatedMetadata = postMotionData(
        client, sessionToken, newMetadata, objectMapper);

    // WHEN a user that can access the camera requests motion data
    ClientResponse response = getMotionData(client, userSessionToken, updatedMetadata.getId());

    // THEN the request is successful
    assertEquals(HttpStatus.OK, response.statusCode());
    // AND the requested metadata is returned
    ImageMetadataV1 returnedMetadata = response
        .bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(ImageMetadataV1.class, objectMapper);
    assertEquals(updatedMetadata, returnedMetadata);
  }

  @Test
  void retrievingMetadataWithImageEntityIsSuccessful() throws InterruptedException {
    // GIVEN a camera is registered to a user and has uploaded motion image data
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);
    UUID cameraId = CameraUtil.createCamera(client, camera, objectMapper);
    String userSessionToken = getSessionToken();
    claimCamera(client, userSessionToken, cameraId, "Camera A");
    String sessionToken = getCameraToken(cameraId, userPassword, objectMapper);
    ImageMetadataV1 newMetadata = new ImageMetadataV1(
        null, null, ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(),
        null, null, false, null);
    ImageMetadataV1 receivedMetadata = postMotionData(client, sessionToken, newMetadata)
        .bodyToMono(OutgoingDataV1.class).block().parseData(ImageMetadataV1.class, objectMapper);
    URL url = Thread.currentThread().getContextClassLoader().getResource("motion/imageA.jpeg");
    File file = new File(url.getPath());
    patchMotionData(client, sessionToken, receivedMetadata.getId(), file);
    Thread.sleep(2000);
    // WHEN a user that can access the data requests the image
    ClientResponse motionImageData = getMotionImageData(
        client, userSessionToken, receivedMetadata.getId());

    // THEN upload is successful
    assertEquals(HttpStatus.OK, motionImageData.statusCode());
  }
}
