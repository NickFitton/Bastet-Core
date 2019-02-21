package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.entity.UserGroup;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.model.GroupData;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles( {"local", "h2"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ImageStorageApplication.class)
public class GroupServiceIT {

  @Autowired
  UserService userService;

  @Autowired
  CameraService cameraService;

  @Autowired
  GroupService groupService;

  @Autowired
  Validator validator;

  @Autowired
  PasswordEncoder encoder;

  private static GroupData createGroup(UUID ownerId) {
    return GroupData.Builder.newBuilder()
        .withGroup(new Group(null, ownerId, "Test Group " + UUID.randomUUID().toString())).build();
  }

  @Test
  public void aUserCanCreateAGroup() {
    User userA = generateUser();

    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();

    Assertions.assertNotNull(savedGroup);
    Assertions.assertEquals(0, savedGroup.getCameraIds().size());
    Assertions.assertEquals(1, savedGroup.getUserIds().size());
    Assertions.assertEquals(savedUserA.getId(), savedGroup.getGroup().getOwnerId());
    Assertions.assertTrue(savedGroup.getUserIds().contains(savedUserA.getId()));
  }

  @Test
  public void usersCanBeAddedToAGroup() {
    User userA = generateUser();
    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    User userB = generateUser();
    User savedUserB = userService.save(userB).block();
    Assertions.assertNotNull(savedUserB);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();
    Assertions.assertNotNull(savedGroup);

    GroupData updatedData = groupService
        .addUserToGroup(userB.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(updatedData);
    Assertions.assertEquals(2, updatedData.getUserIds().size());
    Assertions.assertEquals(savedGroup.getGroup().getId(), updatedData.getGroup().getId());
    Assertions.assertTrue(updatedData.getUserIds().contains(savedUserB.getId()));
    Assertions.assertTrue(updatedData.getUserIds().contains(savedUserA.getId()));
    Assertions.assertNotSame(updatedData.getGroup().getOwnerId(), savedUserB.getId());
  }

  @Test
  public void groupMemberCanBeRemovedFromGroup() {
    User userA = generateUser();
    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    User userB = generateUser();
    User savedUserB = userService.save(userB).block();
    Assertions.assertNotNull(savedUserB);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();
    Assertions.assertNotNull(savedGroup);

    GroupData updatedData = groupService
        .addUserToGroup(userB.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(updatedData);

    updatedData = groupService
        .removeUserFromGroup(savedUserB.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(updatedData);
    Assertions.assertEquals(1, updatedData.getUserIds().size());
  }

  @Test
  public void groupOwnerCanBeChanged() {
    User userA = generateUser();
    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    User userB = generateUser();
    User savedUserB = userService.save(userB).block();
    Assertions.assertNotNull(savedUserB);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();
    Assertions.assertNotNull(savedGroup);

    GroupData updatedData = groupService
        .addUserToGroup(userB.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(updatedData);

    updatedData = groupService.changeOwnerOfGroup(savedUserB.getId(), savedGroup.getGroup().getId())
        .block();
    Assertions.assertNotNull(updatedData);
    Assertions.assertEquals(savedUserB.getId(), updatedData.getGroup().getOwnerId());
  }

  @Test
  public void aUserCanRetrieveAListOfGroupsTheyAreIn() {
    User userA = generateUser();

    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    List<UUID> groupIdList = IntStream.range(1, 4)
        .mapToObj(i -> createGroup(savedUserA.getId()))
        .map(group -> groupService.createGroup(group).block())
        .peek(Assertions::assertNotNull)
        .map(GroupData::getGroup)
        .map(Group::getId).collect(Collectors.toList());

    List<UUID> groupIds = groupService.getGroupsByUserId(savedUserA.getId())
        .map(UserGroup::getGroupId).collectList().block();
    Assertions.assertNotNull(groupIds);

    List<UUID> collect = groupIdList.stream()
        .peek(groupId -> Assertions.assertTrue(groupIds.contains(groupId)))
        .collect(Collectors.toList());
    Assertions.assertEquals(collect.size(), groupIds.size());
  }

  @Test
  public void cameraCanBeAddedToGroup() {
    User userA = generateUser();

    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();
    Assertions.assertNotNull(savedGroup);

    Camera cameraA = generateCamera();
    Camera savedCamera = cameraService.save(cameraA).block();
    Assertions.assertNotNull(savedCamera);

    savedCamera = cameraService
        .updateCamera(savedCamera.getId(), savedUserA.getId(), generateCamera("Test Camera"))
        .block();
    Assertions.assertNotNull(savedCamera);

    savedGroup = groupService
        .addCameraToGroup(savedUserA.getId(), savedCamera.getId(), savedGroup.getGroup().getId())
        .block();
    Assertions.assertNotNull(savedGroup);
    Assertions.assertEquals(1, savedGroup.getCameraIds().size());
    Assertions.assertTrue(savedGroup.getCameraIds().contains(savedCamera.getId()));
  }

  @Test
  public void cameraCanBeRemovedFromGroup() {
    User userA = generateUser();

    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();
    Assertions.assertNotNull(savedGroup);

    Camera cameraA = generateCamera();
    Camera savedCamera = cameraService.save(cameraA).block();
    Assertions.assertNotNull(savedCamera);

    savedCamera = cameraService
        .updateCamera(savedCamera.getId(), savedUserA.getId(), generateCamera("Test Camera"))
        .block();
    Assertions.assertNotNull(savedCamera);

    savedGroup = groupService
        .addCameraToGroup(savedUserA.getId(), savedCamera.getId(), savedGroup.getGroup().getId())
        .block();
    Assertions.assertNotNull(savedGroup);

    savedGroup = groupService
        .removeCameraFromGroup(savedCamera.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(savedGroup);
    Assertions.assertEquals(0, savedGroup.getCameraIds().size());
    Assertions.assertFalse(savedGroup.getCameraIds().contains(savedCamera.getId()));
  }

  @Test
  public void whenGroupMemberIsRemovedCamerasRemovedAlso() {
    User userA = generateUser();
    User savedUserA = userService.save(userA).block();
    Assertions.assertNotNull(savedUserA);

    User userB = generateUser();
    User savedUserB = userService.save(userB).block();
    Assertions.assertNotNull(savedUserB);

    GroupData savedGroup = groupService.createGroup(createGroup(savedUserA.getId())).block();
    Assertions.assertNotNull(savedGroup);

    GroupData updatedData = groupService
        .addUserToGroup(userB.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(updatedData);

    Camera cameraB = generateCamera();
    Camera savedCamera = cameraService.save(cameraB).block();
    Assertions.assertNotNull(savedCamera);

    savedCamera = cameraService
        .updateCamera(savedCamera.getId(), savedUserB.getId(), generateCamera("Test Camera"))
        .block();
    Assertions.assertNotNull(savedCamera);

    savedGroup = groupService
        .addCameraToGroup(savedUserB.getId(), savedCamera.getId(), savedGroup.getGroup().getId())
        .block();
    Assertions.assertNotNull(savedGroup);

    updatedData = groupService
        .removeUserFromGroup(savedUserB.getId(), savedGroup.getGroup().getId()).block();
    Assertions.assertNotNull(updatedData);
    Assertions.assertEquals(1, updatedData.getUserIds().size());
    Assertions.assertEquals(0, updatedData.getCameraIds().size());
  }

  private User generateUser() {
    return AccountMapper.newAccount(new UserV1(
        null,
        "Test",
        "User",
        UUID.randomUUID().toString() + "@prod.com",
        "123456",
        null,
        null,
        null), encoder, validator);
  }

  private Camera generateCamera() {
    return CameraMapper
        .toEntity(
            new CameraV1(
                null,
                null,
                null,
                "123456",
                null,
                null,
                null),
            encoder,
            validator);
  }

  private Camera generateCamera(String name) {
    return CameraMapper
        .toEntity(
            new CameraV1(
                null,
                null,
                name,
                null,
                null,
                null,
                null));
  }
}
