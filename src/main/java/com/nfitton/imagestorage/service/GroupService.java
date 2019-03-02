package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.entity.UserGroup;
import com.nfitton.imagestorage.model.GroupData;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupService {

  Mono<GroupData> createGroup(GroupData groupData);

  Mono<Boolean> existsById(UUID groupId);

  Mono<GroupData> findGroupDataById(UUID groupId);

  Mono<Group> findGroupById(UUID groupId);

  Mono<GroupData> changeOwnerOfGroup(UUID newOwnerId, UUID groupId);

  Mono<GroupData> addUserToGroup(UUID userId, UUID groupId);

  Mono<GroupData> removeUserFromGroup(UUID userId, UUID groupId);

  Flux<UserGroup> getGroupsByUserId(UUID userId);

  Mono<GroupData> addCameraToGroup(UUID requestorId, UUID cameraId, UUID groupId);

  Mono<GroupData> removeCameraFromGroup(UUID cameraId, UUID groupId);

  Mono<Boolean> deleteGroup(UUID groupId);
}
