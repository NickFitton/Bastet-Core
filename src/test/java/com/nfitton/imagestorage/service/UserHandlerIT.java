package com.nfitton.imagestorage.service;

import static com.nfitton.imagestorage.util.GroupUtil.createGroup;
import static com.nfitton.imagestorage.util.GroupUtil.getGroups;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nfitton.imagestorage.api.GroupV1;
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

class UserHandlerIT extends BaseClientIT {

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
  void creatingValidUserIsSuccessful() {
    WebClient client = getWebClient();
    UserV1 user = UserUtil.generateUser().build();

    ClientResponse response = UserUtil.createUser(client, user);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(retrievedData);

    UserV1 userV1 = retrievedData.parseData(UserV1.class, objectMapper);

    assertNotNull(retrievedData);
    assertEquals(user.getFirstName().toLowerCase(), userV1.getFirstName());
    assertEquals(user.getLastName().toLowerCase(), userV1.getLastName());
    assertEquals(user.getEmail(), userV1.getEmail());
    assertNotNull(userV1.getId());
    assertNotNull(userV1.getCreatedAt());
    assertNotNull(userV1.getUpdatedAt());
    assertNotNull(userV1.getLastActive());
    Assertions.assertNull(userV1.getPassword());
  }

  @Test
  void getUsersIsSuccessful() {
    WebClient client = getWebClient();
    ClientResponse response = UserUtil.getUsers(client);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.statusCode());
    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();

    assertNotNull(retrievedData);
    List<UUID> users = retrievedData.parseData(new TypeReference<List<UUID>>() {
    }, objectMapper);

    assertNotNull(users);
    assertTrue(users.size() > 1);

    assertTrue(users.contains(standardUser.getId()));
    assertTrue(users.contains(standardUser.getId()));
  }

  @Test
  void getUserIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    OutgoingDataV1 dataV1 = UserUtil.createUser(client, UserUtil.generateUser().build())
        .bodyToMono(OutgoingDataV1.class).block();
    UserV1 savedUser = dataV1.parseData(UserV1.class, objectMapper);
    ClientResponse response = UserUtil.getUser(client, sessionToken, savedUser.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(retrievedData);

    UserV1 retrievedUser = retrievedData.parseData(UserV1.class, objectMapper);
    assertNotNull(retrievedUser);
    assertEquals(savedUser.getEmail(), retrievedUser.getEmail());
    assertEquals(savedUser.getFirstName(), retrievedUser.getFirstName());
    assertEquals(savedUser.getLastName(), retrievedUser.getLastName());
  }

  @Test
  void getUserSelfIsSuccessful() {
    WebClient client = getWebClient();

    UserV1 newUser = UserUtil.generateUser().build();
    OutgoingDataV1 dataV1 = UserUtil.createUser(client, newUser).bodyToMono(OutgoingDataV1.class)
        .block();
    assertNotNull(dataV1);
    UserV1 savedUser = dataV1.parseData(UserV1.class, objectMapper);
    String savedUserSessionToken = getSessionToken(
        newUser.getEmail(), newUser.getPassword(), objectMapper);
    ClientResponse response = UserUtil.getUser(client, savedUserSessionToken, savedUser.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.statusCode());

    OutgoingDataV1 retrievedData = response.bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(retrievedData);

    UserV1 retrievedUser = retrievedData.parseData(UserV1.class, objectMapper);
    assertNotNull(retrievedUser);
    assertEquals(savedUser.getEmail(), retrievedUser.getEmail());
    assertEquals(savedUser.getFirstName(), retrievedUser.getFirstName());
    assertEquals(savedUser.getLastName(), retrievedUser.getLastName());
  }

  @Test
  void deleteUserIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(standardUser.getEmail(), userPassword, objectMapper);

    UserV1 newUserData = UserUtil.generateUser().build();
    OutgoingDataV1 dataV1 = UserUtil.createUser(client, newUserData)
        .bodyToMono(OutgoingDataV1.class).block();
    String newUserSessionToken = getSessionToken(
        newUserData.getEmail(), newUserData.getPassword(), objectMapper);
    UserV1 savedUser = dataV1.parseData(UserV1.class, objectMapper);
    ClientResponse response = UserUtil.deleteUser(client, newUserSessionToken, savedUser.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.statusCode());

    response = UserUtil.getUser(client, sessionToken, savedUser.getId());

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
  }

  @Test
  void duplicatingUsersFails() {
    WebClient client = getWebClient();
    UserV1 user = UserUtil.generateUser().build();

    ClientResponse response = UserUtil.createUser(client, user);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.statusCode());

    response = UserUtil.createUser(client, user);

    assertNotNull(response);
    assertEquals(HttpStatus.CONFLICT, response.statusCode());
  }

  /**
   * Tests invalid users should fail to be created.
   */
  @ParameterizedTest
  @MethodSource("invalidUsers")
  void invalidUserFails(Builder userBuilder) {
    WebClient client = getWebClient();

    ClientResponse response = UserUtil.createUser(client, userBuilder.build());
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
  }

  @Test
  void existingUserCanGetGroupsTheyArePartOf() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    GroupV1 savedGroup = createGroup(client, sessionToken, objectMapper);

    ClientResponse response = getGroups(client, sessionToken, standardUser.getId());
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.statusCode());

    OutgoingDataV1 receivedData = response.bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(receivedData);
    List<GroupV1> groups = receivedData.parseData(
        new TypeReference<List<GroupV1>>() {
        },
        objectMapper);
    assertNotNull(groups);
    assertEquals(1, groups.size());
    assertEquals(savedGroup, groups.get(0));
  }

  @Test
  void userCanNotGetOtherUsersGroups() {
    // GIVEN a user exists and is part of a group
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();
    createGroup(client, sessionToken, objectMapper);

    // WHEN a different user tries to get the first users group
    UserV1 maliciousUser = UserUtil.createUser(client, objectMapper);
    sessionToken = getSessionToken(
        maliciousUser.getEmail(), maliciousUser.getPassword(), objectMapper);
    ClientResponse response = getGroups(client, sessionToken, standardUser.getId());

    // THEN the server returns forbidden
    assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
  }
}
