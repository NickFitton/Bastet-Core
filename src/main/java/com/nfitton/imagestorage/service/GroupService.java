package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Group;
import com.nfitton.imagestorage.entity.GroupCamera;
import com.nfitton.imagestorage.entity.UserGroup;
import com.nfitton.imagestorage.model.GroupData;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupService {

  Mono<GroupData> createGroup(GroupData groupData);

  Mono<GroupData> findGroupDataById(UUID groupId);

  Mono<Group> findGroupById(UUID groupId);

  Mono<GroupData> changeOwnerOfGroup(UUID newOwnerId, UUID groupId);

  Mono<GroupData> addUserToGroup(UUID userId, UUID groupId);

  Mono<GroupData> removeUserFromGroup(UUID userId, UUID groupId);

  Flux<UserGroup> getGroupsByUserId(UUID userId);

  Flux<GroupCamera> getCamerasByGroupId(UUID userId, UUID groupId);

  Mono<GroupData> removeCameraFromGroup(UUID userId, UUID cameraId, UUID groupId);
}
