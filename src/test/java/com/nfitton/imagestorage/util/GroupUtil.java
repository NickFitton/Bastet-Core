package com.nfitton.imagestorage.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import java.util.UUID;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class GroupUtil {

  public static ClientResponse createGroup(
      WebClient client, String sessionToken, String groupName) {
    GroupV1 newGroup = new GroupV1(null, null, groupName, null, null);
    return client.post()
        .uri("/v1/groups")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .syncBody(newGroup)
        .exchange()
        .block();
  }

  public static ClientResponse getGroups(WebClient client, String sessionToken, UUID userId) {
    return client.get()
        .uri("/v1/users/" + userId + "/groups")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static GroupV1 createGroup(
      WebClient client, String sessionToken, ObjectMapper objectMapper) {
    OutgoingDataV1 receivedData =
        createGroup(client, sessionToken, UUID.randomUUID().toString())
            .bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(receivedData);
    return receivedData.parseData(GroupV1.class, objectMapper);
  }

  public static ClientResponse addUserToGroup(
      WebClient client,
      String sessionToken,
      UUID groupId,
      UserV1 user) {
    return client.post()
        .uri("/v1/groups/" + groupId + "/member")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .syncBody(user)
        .exchange()
        .block();
  }

  public static ClientResponse getGroup(WebClient client, String sessionToken, UUID groupId) {
    return client.get()
        .uri("/v1/groups/" + groupId)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static GroupV1 getGroup(
      WebClient client, String sessionToken, UUID groupId, ObjectMapper mapper) {
    return getGroup(client, sessionToken, groupId)
        .bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(GroupV1.class, mapper);
  }

  public static ClientResponse removeMember(
      WebClient client, String sessionToken, UUID groupId, UUID userId) {
    return client.delete()
        .uri("/v1/groups/" + groupId + "/member/" + userId)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse changeOwnerOfGroup(
      WebClient client, String sessionToken, UUID groupId, UUID newOwner) {
    return client.patch()
        .uri("/v1/groups/" + groupId + "/owner/" + newOwner)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static GroupV1 addCameraToGroup(
      WebClient client, String sessionToken, UUID groupId, UUID cameraId, ObjectMapper mapper) {
    return addCameraToGroup(client, sessionToken, groupId, cameraId)
        .bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(GroupV1.class, mapper);
  }

  public static ClientResponse addCameraToGroup(
      WebClient client, String sessionToken, UUID groupId, UUID cameraId) {
    return client.post()
        .uri("/v1/groups/" + groupId + "/cameras/" + cameraId)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse removeCameraFromGroup(
      WebClient client, String sessionToken, UUID groupId, UUID cameraId) {
    return client.delete()
        .uri("/v1/groups/" + groupId + "/cameras/" + cameraId)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse groupCameras(
      WebClient client, String sessionToken, UUID groupId) {
    return client.get()
        .uri("/v1/groups/" + groupId + "/cameras")
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }

  public static ClientResponse deleteGroup(
      WebClient client, String sessionToken, UUID groupId) {
    return client.delete()
        .uri("/v1/groups/" + groupId)
        .header(HttpHeaders.AUTHORIZATION, "Token " + sessionToken)
        .exchange()
        .block();
  }
}
