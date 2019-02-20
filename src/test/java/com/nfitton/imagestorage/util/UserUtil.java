package com.nfitton.imagestorage.util;

import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.api.UserV1.Builder;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class UserUtil {

  public static ClientResponse createUser(WebClient client, UserV1 user) {
    System.out.println(user.getEmail());
    return client.post()
        .uri("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .syncBody(user)
        .exchange()
        .block();
  }

  public static Builder generateUser() {
    return UserV1.Builder
        .newBuilder()
        .withFirstName("John")
        .withLastName("Doe")
        .withEmail(StringUtil.randomString(10) + "-test@nfitton.com")
        .withPassword("123456");
  }
}
