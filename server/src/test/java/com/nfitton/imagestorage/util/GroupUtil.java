package com.nfitton.imagestorage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import java.util.LinkedList;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class GroupUtil {

  /**
   * Creates a group for the session user, creates users and adds them to the group then returns the
   * populated group.
   *
   * @param client       the web client to interact with the server with
   * @param sessionToken the session token of the logged in user
   * @param objectMapper the mapper for converting outgoing data beans
   * @param extraMembers how many extra members in the group there should be
   * @return a group with extraMembers + 1 members
   */
  public static GroupV1 createAndPopulateGroup(
      WebClient client,
      String sessionToken,
      ObjectMapper objectMapper, int extraMembers) {
    GroupV1 newGroup = createGroup(client, sessionToken, objectMapper);

    LinkedList<UUID> addedUserIds = new LinkedList<>();
    for (int i = 0; i < extraMembers; i++) {
      UserV1 user = UserUtil.createUser(client, objectMapper);
      addedUserIds.add(user.getId());
      ClientResponse response = GroupUtil
          .addUserToGroup(client, sessionToken, newGroup.getId(), user);
      assertEquals(HttpStatus.ACCEPTED, response.statusCode());
    }

    GroupV1 filledGroup = getGroup(client, sessionToken, newGroup.getId(), objectMapper);
    assertNotNull(filledGroup);
    assertEquals(extraMembers + 1, filledGroup.getUsers().size());
    assertEquals(filledGroup.getId(), newGroup.getId());
    assertTrue(filledGroup.getUsers().containsAll(addedUserIds));

    return filledGroup;
  }

  public static GroupV1 createGroup(
      WebClient client, String sessionToken, ObjectMapper objectMapper) {
    OutgoingDataV1 receivedData =
        createGroup(client, sessionToken, UUID.randomUUID().toString())
            .bodyToMono(OutgoingDataV1.class).block();
    assertNotNull(receivedData);
    return receivedData.parseData(GroupV1.class, objectMapper);
  }

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
