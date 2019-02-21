package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.util.CameraUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles( {"test", "local"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ImageStorageApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class CameraHandlerIT extends BaseTestIT {

  @Test
  public void creatingValidCameraIsSuccessful() {
    WebClient client = getWebClient();
    CameraV1 camera = CameraUtil.passwordOnly(userPassword);

    ClientResponse response = CameraUtil.createCamera(client, camera);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.CREATED, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(retrievedData);

    CameraV1 cameraV1 = retrievedData.parseData(CameraV1.class, objectMapper);
    Assertions.assertNull(cameraV1.getPassword());
    Assertions.assertNotNull(cameraV1.getId());

  }

  @Test
  public void getCameraIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    ClientResponse response = CameraUtil.getCamera(client, sessionToken, standardCamera.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
    OutgoingDataV1 retrievedCameraData = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(retrievedCameraData);

    CameraV1 cameraV1 = retrievedCameraData.parseData(CameraV1.class, objectMapper);
    Assertions.assertEquals(standardCamera.getId(), cameraV1.getId());
    Assertions.assertNull(cameraV1.getPassword());
  }

  @Test
  public void deleteCameraIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    ClientResponse response = CameraUtil.deleteCamera(client, sessionToken, standardCamera.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode());

    response = CameraUtil.getCamera(client, sessionToken, standardCamera.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
  }
}
