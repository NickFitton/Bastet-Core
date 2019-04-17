package com.nfitton.imagestorage.service;

import static com.nfitton.imagestorage.util.GroupUtil.addCameraToGroup;
import static com.nfitton.imagestorage.util.GroupUtil.addUserToGroup;
import static com.nfitton.imagestorage.util.GroupUtil.changeOwnerOfGroup;
import static com.nfitton.imagestorage.util.GroupUtil.createGroup;
import static com.nfitton.imagestorage.util.GroupUtil.deleteGroup;
import static com.nfitton.imagestorage.util.GroupUtil.getGroup;
import static com.nfitton.imagestorage.util.GroupUtil.groupCameras;
import static com.nfitton.imagestorage.util.GroupUtil.removeCameraFromGroup;
import static com.nfitton.imagestorage.util.GroupUtil.removeMember;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.util.CameraUtil;
import com.nfitton.imagestorage.util.GroupUtil;
import com.nfitton.imagestorage.util.UserUtil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

class GroupHandlerIT extends BaseClientIT {

  private static GroupV1 createAndPopulateGroup(
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

  @Test
  void userCanCreateGroup() {
    // GIVEN a user exists
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    // WHEN they register a group
    ClientResponse groupA = createGroup(client, sessionToken, "Group A");

    // THEN the group is successfully registered
    assertEquals(HttpStatus.CREATED, groupA.statusCode());
    GroupV1 savedGroup = groupA
        .bodyToMono(OutgoingDataV1.class)
        .map(data -> data.parseData(GroupV1.class, objectMapper))
        .block();
    assertNotNull(savedGroup);
    assertEquals(0, savedGroup.getCameras().size());
    assertEquals(1, savedGroup.getUsers().size());
    assertEquals(standardUser.getId(), savedGroup.getOwnedBy());
    assertTrue(savedGroup.getUsers().contains(standardUser.getId()));
  }

  @Test
  void usersCanBeAddedToAGroup() {
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();
    GroupV1 newGroup = createGroup(client, sessionToken, objectMapper);

    Collection<UUID> addedUserIds = new LinkedList<>();
    for (int i = 0; i < 5; i++) {
      UserV1 user = UserUtil.createUser(client, objectMapper);
      addedUserIds.add(user.getId());
      ClientResponse response = GroupUtil
          .addUserToGroup(client, sessionToken, newGroup.getId(), user);
      assertEquals(HttpStatus.ACCEPTED, response.statusCode());
    }

    GroupV1 filledGroup = getGroup(client, sessionToken, newGroup.getId(), objectMapper);
    assertNotNull(filledGroup);
    assertEquals(6, filledGroup.getUsers().size());
    assertEquals(filledGroup.getId(), newGroup.getId());
    assertTrue(filledGroup.getUsers().containsAll(addedUserIds));
  }

  @Test
  void groupMemberCanBeRemovedFromGroup() {
    // GIVEN a group with 6 members
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();
    GroupV1 newGroup = createGroup(client, sessionToken, objectMapper);

    LinkedList<UUID> addedUserIds = new LinkedList<>();
    for (int i = 0; i < 5; i++) {
      UserV1 user = UserUtil.createUser(client, objectMapper);
      addedUserIds.add(user.getId());
      ClientResponse response = GroupUtil
          .addUserToGroup(client, sessionToken, newGroup.getId(), user);
      assertEquals(HttpStatus.ACCEPTED, response.statusCode());
    }

    GroupV1 filledGroup = getGroup(client, sessionToken, newGroup.getId(), objectMapper);
    assertNotNull(filledGroup);
    assertEquals(6, filledGroup.getUsers().size());
    assertEquals(filledGroup.getId(), newGroup.getId());
    assertTrue(filledGroup.getUsers().containsAll(addedUserIds));

    // WHEN a member is removed
    ClientResponse deleteResponse = GroupUtil
        .removeMember(client, sessionToken, newGroup.getId(), addedUserIds.pop());
    GroupV1 updatedGroup = getGroup(client, sessionToken, newGroup.getId(), objectMapper);

    // THEN the request is successful and the user is no longer listed
    assertEquals(HttpStatus.ACCEPTED, deleteResponse.statusCode());
    assertEquals(addedUserIds.size() + 1, updatedGroup.getUsers().size());
  }

  @Test
  void groupOwnerCanBeChanged() {
    // GIVEN an existing group
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    GroupV1 group = createAndPopulateGroup(client, sessionToken, objectMapper, 5);

    // WHEN the admin changes to a different member
    UUID nonAdminMemberId = group.getUsers().stream().filter(id -> !id.equals(standardUser.getId()))
        .collect(Collectors.toList()).get(0);
    ClientResponse response = changeOwnerOfGroup(
        client, sessionToken, group.getId(), nonAdminMemberId);

    // THEN the request is accepted and the given member is now admin
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());
    GroupV1 updatedGroup = response.bodyToMono(OutgoingDataV1.class).block()
        .parseData(GroupV1.class, objectMapper);
    assertNotEquals(standardUser.getId(), updatedGroup.getOwnedBy());
    assertEquals(nonAdminMemberId, updatedGroup.getOwnedBy());
  }

  @Test
  void cameraCanBeAddedToGroup() {
    // GIVEN a preexisting group and a user that has registered a camera
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    GroupV1 group = createAndPopulateGroup(client, sessionToken, objectMapper, 5);
    CameraUtil.claimCamera(client, sessionToken, standardCamera.getId(), "Camera A");

    // WHEN the user adds the camera to the group
    ClientResponse response = addCameraToGroup(
        client, sessionToken, group.getId(), standardCamera.getId());

    // THEN the addition is successful
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());
    // AND the camera id is added to the group information
    GroupV1 updatedGroup = response
        .bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(GroupV1.class, objectMapper);
    assertTrue(updatedGroup.getCameras().contains(standardCamera.getId()));
  }

  @Test
  void cameraCanBeRemovedFromGroup() {
    // GIVEN a preexisting group with a registered camera
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    GroupV1 group = createAndPopulateGroup(client, sessionToken, objectMapper, 5);
    CameraUtil.claimCamera(client, sessionToken, standardCamera.getId(), "Camera A");
    addCameraToGroup(client, sessionToken, group.getId(), standardCamera.getId());

    // WHEN the camera is removed
    ClientResponse response = removeCameraFromGroup(
        client, sessionToken, group.getId(), standardCamera.getId());

    // THEN the request is successful
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());
    // AND the camera id is not in the group information anymore
    GroupV1 updatedGroup = response.bodyToMono(OutgoingDataV1.class).block()
        .parseData(GroupV1.class, objectMapper);
    assertFalse(updatedGroup.getCameras().contains(standardCamera.getId()));
  }

  @Test
  void whenGroupMemberIsRemovedCamerasRemovedAlso() {
    // GIVEN a preexisting group with a registered camera
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    GroupV1 group = createAndPopulateGroup(client, sessionToken, objectMapper, 0);
    UserV1 removableUser = UserUtil.createUser(client, objectMapper);
    addUserToGroup(client, sessionToken, group.getId(), removableUser);
    String remUserSessionToken = getSessionToken(
        removableUser.getEmail(), removableUser.getPassword(), objectMapper);
    CameraUtil.claimCamera(client, remUserSessionToken, standardCamera.getId(), "Camera A");
    GroupV1 updatedGroup = addCameraToGroup(
        client, remUserSessionToken, group.getId(), standardCamera.getId(), objectMapper);
    assertEquals(2, updatedGroup.getUsers().size());
    assertEquals(1, updatedGroup.getCameras().size());

    // WHEN the user that owns the camera is removed from the group
    ClientResponse response = removeMember(
        client, sessionToken, group.getId(), removableUser.getId());

    // THEN the request is successful
    assertEquals(HttpStatus.ACCEPTED, response.statusCode());

    // AND the users camera is also removed from the group
    updatedGroup = response.bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(GroupV1.class, objectMapper);
    assertEquals(0, updatedGroup.getCameras().size());
  }

  @Test
  void groupWithCamerasCanRetrieveTheDetailsOfCameras() {
    // GIVEN a preexisting group with a registered camera
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();

    GroupV1 group = createAndPopulateGroup(client, sessionToken, objectMapper, 5);
    CameraUtil.claimCamera(client, sessionToken, standardCamera.getId(), "Camera A");
    addCameraToGroup(client, sessionToken, group.getId(), standardCamera.getId());

    // WHEN the camera list is retrieved
    ClientResponse response = groupCameras(client, sessionToken, group.getId());

    // THEN the request is successful
    assertEquals(HttpStatus.OK, response.statusCode());

    // AND the camera information is retrieved
    List<CameraV1> groupCameras = response.bodyToMono(OutgoingDataV1.class)
        .block()
        .parseData(new TypeReference<List<CameraV1>>() {
        }, objectMapper);
    assertTrue(groupCameras.stream().anyMatch(cameraV1 -> cameraV1.getId().equals(standardCamera.getId())));
  }

  @Test
  void whenGroupIsDeletedDataCanNotBeAccessed() {
    // GIVEN an existing group
    WebClient client = getWebClient();
    String sessionToken = getSessionToken();
    GroupV1 group = createAndPopulateGroup(client, sessionToken, objectMapper, 5);

    // WHEN the group is deleted
    ClientResponse deleteResponse = deleteGroup(client, sessionToken, group.getId());
    ClientResponse getResponse = getGroup(client, sessionToken, group.getId());

    // THEN the deletion is successful
    assertEquals(HttpStatus.ACCEPTED, deleteResponse.statusCode());

    // AND further get requests return not found
    assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode());
  }
}
