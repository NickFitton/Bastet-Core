package com.nfitton.imagestorage.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.api.UserV1.Builder;
import com.nfitton.imagestorage.util.StringUtil;
import com.nfitton.imagestorage.util.UserUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class UserHandlerIT extends BaseClientIT {

  private static Stream<Arguments> invalidUsers() {
    return Stream.of(
        Arguments.of(UserUtil.generateUser().withFirstName("has space")),
        Arguments.of(UserUtil.generateUser().withFirstName("num83r5")),
        Arguments.of(UserUtil.generateUser().withFirstName("s")),
        Arguments.of(UserUtil.generateUser().withFirstName(StringUtil.randomString(33))),
        Arguments.of(UserUtil.generateUser().withFirstName(StringUtil.randomString(1))),
        Arguments.of(UserUtil.generateUser().withLastName("has space")),
        Arguments.of(UserUtil.generateUser().withLastName("num83r5")),
        Arguments.of(UserUtil.generateUser().withLastName("s")),
        Arguments.of(UserUtil.generateUser().withLastName(StringUtil.randomString(33))),
        Arguments.of(UserUtil.generateUser().withLastName(StringUtil.randomString(1))),
        Arguments.of(UserUtil.generateUser().withEmail("no.At")),
        Arguments.of(UserUtil.generateUser().withEmail(".at@start")),
        Arguments.of(UserUtil.generateUser().withEmail("dotAt@end.")),
        Arguments.of(UserUtil.generateUser().withEmail("dotAt@.a")),
        Arguments.of(UserUtil.generateUser().withEmail("another.@val")),
        Arguments.of(UserUtil.generateUser().withPassword(StringUtil.randomString(5))),
        Arguments.of(UserUtil.generateUser().withPassword(StringUtil.randomString(65)))
    );
  }

  @Test
  public void creatingValidUserIsSuccessful() {
    WebClient client = getWebClient();
    UserV1 user = UserUtil.generateUser().build();

    ClientResponse response = UserUtil.createUser(client, user);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.CREATED, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(retrievedData);

    UserV1 userV1 = retrievedData.parseData(UserV1.class, objectMapper);

    Assertions.assertNotNull(retrievedData);
    Assertions.assertEquals(user.getFirstName().toLowerCase(), userV1.getFirstName());
    Assertions.assertEquals(user.getLastName().toLowerCase(), userV1.getLastName());
    Assertions.assertEquals(user.getEmail(), userV1.getEmail());
    Assertions.assertNotNull(userV1.getId());
    Assertions.assertNotNull(userV1.getCreatedAt());
    Assertions.assertNotNull(userV1.getUpdatedAt());
    Assertions.assertNotNull(userV1.getLastActive());
    Assertions.assertNull(userV1.getPassword());
  }

  @Test
  public void getUsersIsSuccessful() {
    WebClient client = getWebClient();
    ClientResponse response = UserUtil.getUsers(client);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();

    Assertions.assertNotNull(retrievedData);
    List<UUID> users = retrievedData.parseData(new TypeReference<List<UUID>>() {
    }, objectMapper);

    Assertions.assertNotNull(users);
    Assertions.assertTrue(users.size() > 1);

    Assertions.assertTrue(users.contains(standardUser.getId()));
    Assertions.assertTrue(users.contains(standardUser.getId()));
  }

  @Test
  public void getUserIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    OutgoingDataV1 dataV1 = UserUtil.createUser(client, UserUtil.generateUser().build())
        .bodyToMono(OutgoingDataV1.class).block();
    UserV1 savedUser = dataV1.parseData(UserV1.class, objectMapper);
    ClientResponse response = UserUtil.getUser(client, sessionToken, savedUser.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    Assertions.assertNotNull(retrievedData);

    UserV1 retrievedUser = retrievedData.parseData(UserV1.class, objectMapper);
    Assertions.assertNotNull(retrievedUser);
    Assertions.assertEquals(savedUser.getEmail(), retrievedUser.getEmail());
    Assertions.assertEquals(savedUser.getFirstName(), retrievedUser.getFirstName());
    Assertions.assertEquals(savedUser.getLastName(), retrievedUser.getLastName());
  }

  @Test
  public void deleteUserIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    UserV1 newUserData = UserUtil.generateUser().build();
    OutgoingDataV1 dataV1 = UserUtil.createUser(client, newUserData)
        .bodyToMono(OutgoingDataV1.class).block();
    String newUserSessionToken = getSessionToken(
        newUserData.getEmail(), newUserData.getPassword(), objectMapper);
    UserV1 savedUser = dataV1.parseData(UserV1.class, objectMapper);
    ClientResponse response = UserUtil.deleteUser(client, newUserSessionToken, savedUser.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode());

    response = UserUtil.getUser(client, sessionToken, savedUser.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
  }

  @Test
  public void duplicatingUsersFails() {
    WebClient client = getWebClient();
    UserV1 user = UserUtil.generateUser().build();

    ClientResponse response = UserUtil.createUser(client, user);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.CREATED, response.statusCode());

    response = UserUtil.createUser(client, user);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode());
  }

  /**
   * Tests invalid users should fail to be created.
   */
  @ParameterizedTest
  @MethodSource("invalidUsers")
  public void invalidUserFails(Builder userBuilder) {
    WebClient client = getWebClient();

    ClientResponse response = UserUtil.createUser(client, userBuilder.build());
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
  }
}
