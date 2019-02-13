package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.ImageStorageApplication;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.entity.UserGroup;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.model.GroupData;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles( {"local", "h2"})
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ImageStorageApplication.class)
public class GroupServiceIT {

  @Autowired
  UserService userService;

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
  public void groupOwnerCanRemoveOtherUsersFromGroup() {
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
  public void groupOwnerCanChangeOwnerOfGroup() {
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

  private User generateUser() {
    return AccountMapper.newAccount(new UserV1(
        null,
        "Test",
        "User",
        UUID.randomUUID().toString() + "@prod.com",
        "1234",
        null,
        null,
        null), encoder, validator);
  }
}
