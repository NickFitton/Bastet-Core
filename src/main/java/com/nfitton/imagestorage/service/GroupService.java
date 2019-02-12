package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.model.GroupData;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GroupService {

  Mono<GroupData> createGroup(GroupData groupData);

  Mono<GroupData> findGroupDataById(UUID groupId);

  Mono<GroupData> addUserToGroup(UUID userId, UUID groupId);

  Mono<GroupData> removeUserFromGroup(UUID userId, UUID groupId);

  Mono<GroupData> changeOwnerOfGroup(UUID newOwnerId, UUID groupId);

  Flux<GroupData> getGroupsByUserId(UUID userId);

  Flux<Camera> getCamerasByGroupId(UUID userId, UUID groupId);

  Mono<GroupData> removeCameraFromGroup(UUID userId, UUID cameraId, UUID groupId);
}
