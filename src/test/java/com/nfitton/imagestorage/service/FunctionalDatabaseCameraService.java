package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.service.impl.DatabaseCameraService;
import com.nfitton.imagestorage.service.impl.DatabaseEncryptionService;
import com.nfitton.imagestorage.utility.KeyUtils;

import static com.nfitton.imagestorage.utility.KeyUtils.createKeyAgreement;
import static com.nfitton.imagestorage.utility.KeyUtils.parseEncodedKey;
import static com.nfitton.imagestorage.utility.KeyUtils.toMinHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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

  @Autowired
  DatabaseEncryptionService encryptionService;

  @Test public void handshakingIsSuccessful()
      throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException,
             NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
    Camera testCamera = Camera.Builder.newBuilder().withName("Test Camera").build();
    Camera savedCamera = service.register(testCamera).block();
    assertNotNull(savedCamera);

    CameraV1 apiCamera = CameraMapper.toApiBean(savedCamera);

    assertNotNull(savedCamera);
    UUID cameraId = savedCamera.getId();
    KeyPair keyPair = KeyUtils.generateDHKeyPair();
    KeyAgreement keyAgreement = createKeyAgreement(keyPair);
    String publicKey =
        toMinHexString(keyPair.getPublic().getEncoded());

    String backendPublicKey = service.startHandshake(cameraId, publicKey).block();

    assertNotNull(backendPublicKey);
    byte[] encodedBackendKey = KeyUtils.fromMinHexString(backendPublicKey);

    keyAgreement.doPhase(parseEncodedKey(encodedBackendKey), true);
    byte[] deviceSecret = keyAgreement.generateSecret();
    SecretKeySpec deviceSecretKeySpec = new SecretKeySpec(deviceSecret, 0, 16, "AES");

    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
    String jsonifiedCamera = mapper.writeValueAsString(apiCamera);

    Cipher cipherB = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipherB.init(Cipher.ENCRYPT_MODE, deviceSecretKeySpec);
    byte[] clearText = jsonifiedCamera.getBytes();
    byte[] cipherText = cipherB.doFinal(clearText);
    byte[] parameters = cipherB.getParameters().getEncoded();

    CameraV1 decryptedCamera = encryptionService
        .decryptObject(cameraId,
                       toMinHexString(cipherText),
                       toMinHexString(parameters),
                       CameraV1.class)
        .block();

    System.out.println(decryptedCamera.getCreatedAt());
    System.out.println(savedCamera.getCreatedAt());
    assertEquals(decryptedCamera, apiCamera);
  }
}
