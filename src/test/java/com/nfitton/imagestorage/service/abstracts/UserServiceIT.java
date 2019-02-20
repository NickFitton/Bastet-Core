package com.nfitton.imagestorage.service.abstracts;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.api.UserV1.Builder;
import com.nfitton.imagestorage.util.StringUtil;
import com.nfitton.imagestorage.util.UserUtil;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles( {"test", "local"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ImageStorageApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserServiceIT {

  private static final String BASE_URL = "http://localhost";
  @LocalServerPort
  private int randomServerPort;

  private WebClient getWebClient() {
    return WebClient.create(BASE_URL + ":" + randomServerPort);
  }

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

    OutgoingDataV1 savedUser = response.bodyToMono(OutgoingDataV1.class).block();

    Assertions.assertNotNull(savedUser);
    Assertions.assertTrue(savedUser.getData() instanceof UserV1);
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
    System.out
        .println(response.bodyToMono(OutgoingDataV1.class).map(OutgoingDataV1::getError).block());
  }
}
