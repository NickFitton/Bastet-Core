package com.nfitton.imagestorage.service;

import static com.nfitton.imagestorage.util.StringUtil.randomString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.api.OutgoingDataV1;
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

@ActiveProfiles({"test", "local"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = ImageStorageApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class BaseClientIT {

  private static final String BASE_URL = "http://localhost";
  User standardUser;
  Camera standardCamera;
  String userPassword;
  ObjectMapper objectMapper;
  @Autowired
  PasswordEncoder encoder;
  @LocalServerPort
  private int randomServerPort;
  private WebClient client;
  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private CameraRepository cameraRepository;

  WebClient getWebClient() {
    if (client == null) {
      client = WebClient.create(BASE_URL + ":" + randomServerPort);
    }
    return client;
  }

  @BeforeEach
  void testSetup() {
    objectMapper = new ObjectMapper()
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
    ZonedDateTime now = ZonedDateTime.now();
    userPassword = randomString(8);
    standardUser = User.Builder.newBuilder()
        .withFirstName("john")
        .withLastName("doe")
        .withType(AccountType.BASIC)
        .withEmail(randomString(10) + "@test.com")
        .withPassword(encoder.encode(userPassword))
        .withLastActive(now).build();
    standardUser = accountRepository.save(standardUser);
    standardCamera = Camera.Builder.newBuilder()
        .withPassword(encoder.encode(userPassword))
        .withLastActive(now).build();
    standardCamera = cameraRepository.save(standardCamera);
  }

  protected String getSessionToken(String email, String password, ObjectMapper mapper) {
    ClientResponse response = client
        .post()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getLoginHeader(email, password))
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .block();

    OutgoingDataV1 dataV1 = response.bodyToMono(OutgoingDataV1.class).block();
    return dataV1.parseData(String.class, mapper);
  }

  protected String getLoginHeader(String email, String password) {
    return "Basic " + new String(
        Base64.getEncoder().encode((email + ":" + password).getBytes()));
  }

  protected String getTokenHeader(String token) {
    return "Token " + token;
  }
}
