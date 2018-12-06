package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.service.impl.DatabaseCameraService;
import com.nfitton.imagestorage.utility.KeyUtils;

import static org.junit.Assert.assertNotNull;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "h2"})
@SpringBootTest
public class FunctionalDatabaseCameraService {

  @Autowired
  DatabaseCameraService service;

  @Test public void handshakingIsSuccessful() throws NoSuchAlgorithmException {
    Camera testCamera = Camera.Builder.newBuilder().withName("Test Camera").build();
    Camera savedCamera = service.register(testCamera).block();

    assertNotNull(savedCamera);
    UUID cameraId = savedCamera.getId();
    String publicKey =
        KeyUtils.toMinHexString(KeyUtils.generateDHKeyPair().getPublic().getEncoded());

    service.startHandshake(cameraId, publicKey).block();
  }
}
