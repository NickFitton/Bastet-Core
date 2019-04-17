package com.nfitton.imagestorage.service;

import static com.nfitton.imagestorage.util.CameraUtil.claimCamera;
import static com.nfitton.imagestorage.util.CameraUtil.getCameras;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.util.CameraUtil;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class CameraHandlerIT extends BaseClientIT {

  @Test
  void creatingValidCameraIsSuccessful() {
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);

    ClientResponse response = CameraUtil.createCamera(client, camera);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(retrievedData);

    CameraV1 cameraV1 = retrievedData.parseData(CameraV1.class, objectMapper);
    Assertions.assertNull(cameraV1.getPassword());
    assertNotNull(cameraV1.getId());
  }

  @Test
  void userCanClaimUnclaimedCamera() {
    // GIVEN the user and camera exist
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    // WHEN a user registers a camera that isn't otherwise registered
    ClientResponse response = claimCamera(
        client, sessionToken, standardCamera.getId(), UUID.randomUUID().toString());

    // THEN the registration is successful
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());
  }

  @Test
  void getCameraIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    ClientResponse response = CameraUtil.getCamera(client, sessionToken, standardCamera.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.statusCode());
    OutgoingDataV1 retrievedCameraData = response.bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(retrievedCameraData);

    CameraV1 cameraV1 = retrievedCameraData.parseData(CameraV1.class, objectMapper);
    assertEquals(standardCamera.getId(), cameraV1.getId());
    Assertions.assertNull(cameraV1.getPassword());
  }

  @Test
  void getCameraWithBadUuidFails() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    ClientResponse response = client.get()
        .uri("/v1/cameras/NOT_A_UUID")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();

    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
  }

  @Test
  void userCanRetrieveClaimedCameras() {
    // GIVEN a user has claimed a camera
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();
    ClientResponse response = claimCamera(
        client, sessionToken, standardCamera.getId(), UUID.randomUUID().toString());
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());

    // WHEN a user retrieves a list of their cameras
    response = getCameras(client, sessionToken);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.statusCode());
    List<CameraV1> cameraV1s = response.bodyToMono(OutgoingDataV1.class).block()
        .parseData(new TypeReference<List<CameraV1>>() {
        }, objectMapper);

    assertNotNull(cameraV1s);
    assertEquals(1, cameraV1s.size());
  }

  @Test
  void deleteCameraIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    ClientResponse response = CameraUtil.deleteCamera(client, sessionToken, standardCamera.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.statusCode());

    response = CameraUtil.getCamera(client, sessionToken, standardCamera.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
  }
}
