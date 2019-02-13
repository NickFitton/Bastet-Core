package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.entity.GroupCamera;
import com.nfitton.imagestorage.entity.UserGroup;
import com.nfitton.imagestorage.exception.ForbiddenException;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.model.GroupData;
import com.nfitton.imagestorage.repository.GroupCameraRepository;
import com.nfitton.imagestorage.repository.GroupRepository;
import com.nfitton.imagestorage.repository.UserGroupRepository;
import com.nfitton.imagestorage.service.GroupService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.ExceptionUtil;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;

@Service
public class DatabaseGroupService implements GroupService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseGroupService.class);

  private final GroupRepository groupRepository;
  private final GroupCameraRepository groupCameraRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserService userService;

  @Autowired
  public DatabaseGroupService(
      GroupRepository groupRepository,
      GroupCameraRepository groupCameraRepository,
      UserGroupRepository userGroupRepository,
      UserService userService) {
    this.groupRepository = groupRepository;
    this.groupCameraRepository = groupCameraRepository;
    this.userGroupRepository = userGroupRepository;
    this.userService = userService;
  }

  private static NotFoundException groupNotFound(UUID groupId) {
    return new NotFoundException(String.format("Group not found by id: %s", groupId));
  }

  private static GroupData mapToData(Tuple3<Group, List<UserGroup>, List<GroupCamera>> tuple3) {
    return GroupData.Builder.newBuilder()
        .withGroup(tuple3.getT1()).withUserGroups(tuple3.getT2())
        .withGroupCameras(tuple3.getT3()).build();
  }

  @Override
  public Mono<GroupData> createGroup(GroupData groupData) {
    final UUID userId = groupData.getGroup().getOwnerId();
    return userService.existsById(userId)
        .map(exists -> {
          if (!exists) {
            throw new NotFoundException(String.format("User not found by id: %s", userId));
          }
          return groupData.getGroup();
        })
        .flatMap(this::saveGroup)
        .flatMap(group -> addUserToGroup(userId, group.getId()));
  }

  @Override
  public Mono<GroupData> addUserToGroup(UUID userId, UUID groupId) {
    return Mono.zip(existsById(groupId), userService.existsById(userId))
        .map(tuple2 -> {
          if (!tuple2.getT1()) {
            throw groupNotFound(groupId);
          } else if (!tuple2.getT2()) {
            throw ExceptionUtil.userNotFound();
          }
          return new UserGroup(null, userId, groupId);
        })
        .flatMap(this::saveUserGroup)
        .map(UserGroup::getGroupId)
        .flatMap(this::findGroupDataById);
  }

  @Override
  public Mono<GroupData> removeUserFromGroup(UUID userId, UUID groupId) {
    return Mono.zip(findGroupDataById(groupId), userService.existsById(userId))
        .flatMap(tuple2 -> {
          if (!tuple2.getT2()) {
            throw ExceptionUtil.userNotFound();
          }
          GroupData data = tuple2.getT1();
          if (userId == data.getGroup().getOwnerId()) {
            throw new ForbiddenException("Cannot remove owner from group");
          }
          if (!data.getUserIds().contains(userId)) {
            throw new NotFoundException("User not in group");
          }
          return removeUser(userId, groupId);
        }).flatMap(success -> {
          if (!success) {
            throw new InternalServerException("Failed to remove user from group");
          }
          return findGroupDataById(groupId);
        });
  }

  @Override
  public Mono<GroupData> changeOwnerOfGroup(UUID newOwnerId, UUID groupId) {
    return Mono.zip(findGroupDataById(groupId), userService.existsById(newOwnerId))
        .flatMap(tuple -> {
          if (!tuple.getT2()) {
            throw ExceptionUtil.userNotFound();
          }
          GroupData data = tuple.getT1();
          if (!data.getUserIds().contains(newOwnerId)) {
            throw new NotFoundException("User not in group");
          }
          Group differentOwner = new Group(
              data.getGroup().getId(), newOwnerId, data.getGroup().getName());
          return saveGroup(differentOwner);
        })
        .flatMap(updatedGroup -> findGroupDataById(updatedGroup.getId()));
  }

  @Override
  public Flux<UserGroup> getGroupsByUserId(UUID userId) {
    return Mono.fromCallable(() -> userGroupRepository.findAllByUserId(userId))
        .flatMapIterable(userGroups -> userGroups);
  }

  @Override
  public Flux<GroupCamera> getCamerasByGroupId(
      UUID userId, UUID groupId) {
    return null;
  }

  @Override
  public Mono<GroupData> removeCameraFromGroup(UUID userId, UUID cameraId, UUID groupId) {
    return null;
  }

  private Mono<List<UserGroup>> findUsersInGroup(UUID groupId) {
    return Mono.fromCallable(() -> userGroupRepository.findAllByGroupId(groupId));
  }

  private Mono<List<GroupCamera>> findCamerasInGroup(UUID groupId) {
    return Mono.fromCallable(() -> groupCameraRepository.findAllByGroupId(groupId));
  }

  private Mono<Boolean> existsById(UUID groupId) {
    return Mono.fromCallable(() -> groupRepository.existsById(groupId))
        .subscribeOn(Schedulers.elastic());
  }

  @Override
  public Mono<GroupData> findGroupDataById(UUID groupId) {
    return Mono.zip(findById(groupId), findUsersInGroup(groupId), findCamerasInGroup(groupId))
        .map(DatabaseGroupService::mapToData);
  }

  @Override
  public Mono<Group> findGroupById(UUID groupId) {
    return Mono.fromCallable(() -> groupRepository.findById(groupId)
        .orElseThrow(() -> new NotFoundException("Group not found by given id")));
  }

  private Mono<Group> findById(UUID groupId) {
    return Mono.fromCallable(() -> groupRepository.findById(groupId))
        .map(optionalGroup -> optionalGroup.orElseThrow(() -> groupNotFound(groupId)));
  }

  private Mono<Group> saveGroup(Group group) {
    LOGGER.debug("Saving group: [{}, {}, {}]", group.getId(), group.getName(), group.getOwnerId());
    return Mono.fromCallable(() -> groupRepository.save(group))
        .subscribeOn(Schedulers.elastic());
  }

  private Mono<UserGroup> saveUserGroup(UserGroup userGroup) {
    return Mono.fromCallable(() -> userGroupRepository.save(userGroup))
        .subscribeOn(Schedulers.elastic());
  }

  private Mono<GroupCamera> saveGroupCamera(GroupCamera groupCamera) {
    return Mono.fromCallable(() -> groupCameraRepository.save(groupCamera))
        .subscribeOn(Schedulers.elastic());
  }

  private Mono<Boolean> removeUser(UUID userId, UUID groupId) {
    return Mono.fromCallable(() -> {
      userGroupRepository.deleteByUserIdAndGroupId(userId, groupId);
      return true;
    })
        .doOnError(
            e -> LOGGER
                .error("Threw error when removing user {} from group {}: {}", userId, groupId, e))
        .onErrorReturn(false);
  }
}
