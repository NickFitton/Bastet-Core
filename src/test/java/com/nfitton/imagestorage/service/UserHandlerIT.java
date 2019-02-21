package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.api.UserV1.Builder;
import com.nfitton.imagestorage.util.StringUtil;
import com.nfitton.imagestorage.util.UserUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
public class UserHandlerIT extends BaseTestIT {

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

    UserV1 savedUserData = response.bodyToMono(UserV1.class).block();

    Assertions.assertNotNull(savedUserData);
    Assertions.assertEquals(user.getFirstName().toLowerCase(), savedUserData.getFirstName());
    Assertions.assertEquals(user.getLastName().toLowerCase(), savedUserData.getLastName());
    Assertions.assertEquals(user.getEmail(), savedUserData.getEmail());
    Assertions.assertNotNull(savedUserData.getId());
    Assertions.assertNotNull(savedUserData.getCreatedAt());
    Assertions.assertNotNull(savedUserData.getUpdatedAt());
    Assertions.assertNotNull(savedUserData.getLastActive());
    Assertions.assertNull(savedUserData.getPassword());
  }

  @Test
  public void getUsersIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(adminUser.getEmail(), userPassword);
    ClientResponse response = UserUtil.getUsers(client, sessionToken);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
    List<UUID> users = response.bodyToFlux(UUID.class).collectList().block();

    Assertions.assertNotNull(users);
    Assertions.assertTrue(users.size() > 1);
    Assertions.assertTrue(users.contains(adminUser.getId()));
    Assertions.assertTrue(users.contains(standardUser.getId()));
  }

  @Test
  public void getUserIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(adminUser.getEmail(), userPassword);

    ClientResponse response = UserUtil.getUser(client, sessionToken, standardUser.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.OK, response.statusCode());
    UserV1 retrievedUser = response.bodyToMono(UserV1.class).block();
    Assertions.assertNotNull(retrievedUser);
    Assertions.assertEquals(standardUser.getEmail(), retrievedUser.getEmail());
    Assertions.assertEquals(standardUser.getFirstName(), retrievedUser.getFirstName());
    Assertions.assertEquals(standardUser.getLastName(), retrievedUser.getLastName());
  }

  @Test
  public void deleteUserIsSuccessful() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken(adminUser.getEmail(), userPassword);

    ClientResponse response = UserUtil.deleteUser(client, sessionToken, standardUser.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode());

    response = UserUtil.getUser(client, sessionToken, standardUser.getId());

    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
  }

  @Test
  public void getGroupsForUserIsSuccessful() {

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

  @ParameterizedTest
  @MethodSource("invalidUsers")
  public void invalidUserFails(Builder userBuilder) {
    WebClient client = getWebClient();

    ClientResponse response = UserUtil.createUser(client, userBuilder.build());
    Assertions.assertNotNull(response);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
  }
}
