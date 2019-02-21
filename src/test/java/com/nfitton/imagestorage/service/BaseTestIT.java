package com.nfitton.imagestorage.service;

import static com.nfitton.imagestorage.util.StringUtil.randomString;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.repository.AccountRepository;
import com.nfitton.imagestorage.repository.CameraRepository;
import java.time.ZonedDateTime;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles( {"test", "local"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ImageStorageApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class BaseTestIT {

  private static final String BASE_URL = "http://localhost";
  protected User adminUser;
  protected User standardUser;
  protected Camera standardCamera;
  protected String userPassword;
  @Autowired
  PasswordEncoder encoder;
  @LocalServerPort
  private int randomServerPort;
  private WebClient client;
  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private CameraRepository cameraRepository;

  protected WebClient getWebClient() {
    if (client == null) {
      client = WebClient.create(BASE_URL + ":" + randomServerPort);
    }
    return client;
  }

  @BeforeEach
  void testSetup() {
    ZonedDateTime now = ZonedDateTime.now();
    userPassword = randomString(8);
    adminUser = User.Builder.newBuilder()
        .withFirstName("auth")
        .withLastName("user")
        .withType(AccountType.ADMIN)
        .withEmail(randomString(10) + "@test.com")
        .withPassword(encoder.encode(userPassword))
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .withLastActive(now).build();
    adminUser = accountRepository.save(adminUser);
    standardUser = User.Builder.newBuilder()
        .withFirstName("john")
        .withLastName("doe")
        .withType(AccountType.BASIC)
        .withEmail(randomString(10) + "@test.com")
        .withPassword(encoder.encode(userPassword))
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .withLastActive(now).build();
    standardUser = accountRepository.save(standardUser);
    standardCamera = Camera.Builder.newBuilder()
        .withPassword(encoder.encode(userPassword))
        .withUpdatedAt(now)
        .withCreatedAt(now)
        .withLastActive(now).build();
    standardCamera = cameraRepository.save(standardCamera);
  }

  protected String getSessionToken(String email, String password) {
    ClientResponse response = client
        .post()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getLoginHeader(standardUser.getEmail(), userPassword))
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .block();

    return response.bodyToMono(String.class).block();
  }

  protected String getLoginHeader(String email, String password) {
    return "Basic " + new String(
        Base64.getEncoder().encode((email + ":" + password).getBytes()));
  }

  protected String getTokenHeader(String token) {
    return "Token " + token;
  }
}
