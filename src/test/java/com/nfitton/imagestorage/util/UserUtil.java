package com.nfitton.imagestorage.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.api.UserV1.Builder;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class UserUtil {

  public static UserV1 createUser(WebClient client, ObjectMapper mapper) {
    UserV1 newUser = generateUser().build();
    OutgoingDataV1 receivedData = createUser(client, newUser).bodyToMono(OutgoingDataV1.class)
        .block();
    assertNotNull(receivedData);
    UserV1 savedUser = receivedData.parseData(UserV1.class, mapper);

    return new UserV1(
        savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail(),
        newUser.getPassword(), savedUser.getCreatedAt(), savedUser.getUpdatedAt(),
        savedUser.getLastActive());
  }

  /**
   * Returns a request to create a user given the client and user to save.
   *
   * @param client the client to make the request on
   * @param user the user to save
   * @return the response from the server
   */
  public static ClientResponse createUser(WebClient client, UserV1 user) {
    return client.post()
        .uri("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .syncBody(user)
        .exchange()
        .block();
  }

  /**
   * Returns a request to gather a list of users.
   *
   * @param client the client to make the request on
   * @return the response from the server
   */
  public static ClientResponse getUsers(WebClient client) {
    return client.get()
        .uri("/v1/users")
        .exchange()
        .block();
  }

  /**
   * Returns a request to receive a specific user by their userId.
   *
   * @param client the client to make the request on
   * @param sessionToken the session token of the user making the request
   * @param userId the {@link UUID} to get the user by
   * @return the response from the server
   */
  public static ClientResponse getUser(WebClient client, String sessionToken, UUID userId) {
    return client.get()
        .uri("/v1/users/" + userId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  /**
   * Returns a request to delete a specific user by their userId.
   *
   * @param client the client to make the request on
   * @param sessionToken the session token of the user making the request
   * @param userId the {@link UUID} to identify the user by
   * @return the response from the server
   */
  public static ClientResponse deleteUser(WebClient client, String sessionToken, UUID userId) {
    return client.delete()
        .uri("/v1/users/" + userId.toString())
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  /**
   * Returns a new {@link UserV1.Builder} to save.
   *
   * @return the {@link UserV1.Builder} to manipulate then save
   */
  public static Builder generateUser() {
    return UserV1.Builder
        .newBuilder()
        .withFirstName("John")
        .withLastName("Doe")
        .withEmail(StringUtil.randomString(10) + "-test@nfitton.com")
        .withPassword("123456");
  }
}
