package com.nfitton.imagestorage.service;

import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class LoginHandlerIT extends BaseClientIT {

  @Test
  public void validUserLoginIsSuccessful() {
    WebClient client = getWebClient();

    ClientResponse response = client
        .post()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getLoginHeader(standardUser.getEmail(), userPassword))
        .exchange()
        .block();
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
  }

  @Test
  public void validCameraLoginIsSuccessful() {
    WebClient client = getWebClient();

    ClientResponse response = client
        .post()
        .uri("/v1/login/camera")
        .header(
            HttpHeaders.AUTHORIZATION,
            getLoginHeader(standardCamera.getId().toString(), userPassword))
        .exchange()
        .block();
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
  }

  @Test
  public void invalidCameraIdIsUnsuccessful() {
    WebClient client = getWebClient();

    ClientResponse response = client
        .post()
        .uri("/v1/login/camera")
        .header(
            HttpHeaders.AUTHORIZATION,
            getLoginHeader("notauuid", userPassword))
        .exchange()
        .block();
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
  }

  @Test
  public void invalidUserLoginNotSuccessful() {
    WebClient client = getWebClient();

    ClientResponse response = client
        .post()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getLoginHeader("ususedEmail@nfitton.com", "123456"))
        .exchange()
        .block();
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
  }

  @Test
  public void multipleValidAuthHeadersIsInvalid() {
    WebClient client = getWebClient();

    ClientResponse response = client
        .post()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getLoginHeader(standardUser.getEmail(), userPassword))
        .header(HttpHeaders.AUTHORIZATION, getLoginHeader(standardUser.getEmail(), userPassword))
        .exchange()
        .block();
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode());

    OutgoingDataV1 errorResponse = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(errorResponse);
    Assertions.assertNull(errorResponse.getData());
    Assertions.assertNotNull(errorResponse.getError());
  }

  @Test
  public void malformedHeaderIsInvalid() {
    WebClient client = getWebClient();

    ClientResponse response = client
        .post()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, "Well this is wrong")
        .exchange()
        .block();
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode());

    OutgoingDataV1 errorResponse = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(errorResponse);
    Assertions.assertNull(errorResponse.getData());
    Assertions.assertNotNull(errorResponse.getError());
  }

  @Test
  public void successfulLoginReturnsUsableSessionToken() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);
    System.out.println(sessionToken);

    ClientResponse response = client.get()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader(sessionToken))
        .exchange()
        .block();

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
  }

  @Test
  public void sessionTokenCanBeUsedToGetOwnUser() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    ClientResponse response = client.get()
        .uri("/v1/login/user")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader(sessionToken))
        .exchange()
        .block();

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(retrievedData);

    UserV1 retrievedUser = retrievedData.parseData(UserV1.class, objectMapper);

    Assertions.assertNotNull(retrievedUser);
    Assertions.assertEquals(standardUser.getEmail(), retrievedUser.getEmail());
    Assertions.assertEquals(standardUser.getFirstName(), retrievedUser.getFirstName());
    Assertions.assertEquals(standardUser.getLastName(), retrievedUser.getLastName());
  }
}
