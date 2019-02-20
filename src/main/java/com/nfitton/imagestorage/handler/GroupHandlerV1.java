package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.getUUIDParameter;

import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.exception.ConflictException;
import com.nfitton.imagestorage.exception.ForbiddenException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.exception.VerificationException;
import com.nfitton.imagestorage.mapper.GroupMapper;
import com.nfitton.imagestorage.model.GroupData;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.GroupService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.util.UUID;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class GroupHandlerV1 {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroupHandlerV1.class);

  private final GroupService groupService;
  private final UserService userService;
  private final CameraService cameraService;
  private final AuthenticationService authenticationService;
  private final Validator validator;

  @Autowired
  public GroupHandlerV1(
      GroupService groupService,
      UserService userService,
      CameraService cameraService,
      AuthenticationService authenticationService,
      Validator validator) {
    this.groupService = groupService;
    this.userService = userService;
    this.cameraService = cameraService;
    this.authenticationService = authenticationService;
    this.validator = validator;
  }

  private static boolean userInGroup(GroupData data, UUID userId) {
    return data.getGroup().getOwnerId().equals(userId) || data.getUserIds().contains(userId);
  }

  public Mono<ServerResponse> createGroup(ServerRequest request) {
    return Mono.zip(
        request.bodyToMono(GroupV1.class),
        RouterUtil.parseAuthenticationToken(request, authenticationService))
        .map(tuple2 -> {
          GroupV1 newGroup = tuple2.getT1();
          UUID userId = tuple2.getT2();
          return GroupMapper.newGroup(newGroup, userId, validator);
        })
        .flatMap(groupService::createGroup)
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.status(HttpStatus.CREATED).syncBody(account))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getGroupById(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.findGroupDataById(groupId))
        .map(tuple -> {
          if (tuple.getT2().isInGroup(tuple.getT1())) {
            return tuple.getT2();
          }
          throw new ForbiddenException("User does not belong to group");
        })
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.status(HttpStatus.OK).syncBody(account))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> removeUserFromGroup(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");
    UUID userId = getUUIDParameter(request, "userId");

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.findGroupDataById(groupId))
        .flatMap(tuple -> {
          UUID requestingUser = tuple.getT1();
          GroupData group = tuple.getT2();
          if (group.getGroup().getOwnerId() == requestingUser || userId == requestingUser) {
            return groupService.removeUserFromGroup(userId, groupId);
          }
          throw new ForbiddenException("Must be owner to remove other users");
        })
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> changeOwnerOfGroup(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");
    UUID newOwnerId = getUUIDParameter(request, "userId");

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.findGroupDataById(groupId))
        .flatMap(tuple -> {
          UUID requestingUser = tuple.getT1();
          GroupData group = tuple.getT2();
          if (group.getGroup().getOwnerId() == requestingUser) {
            return groupService.changeOwnerOfGroup(newOwnerId, groupId);
          }
          throw new ForbiddenException("Must be owner to change owner");
        })
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> addCameraToGroup(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");
    UUID cameraId = getUUIDParameter(request, "cameraId");

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.existsById(groupId))
        .flatMap(tuple -> {
          if (!tuple.getT2()) {
            throw new NotFoundException("Group not found by given id");
          }
          UUID requestingUser = tuple.getT1();

          return groupService.addCameraToGroup(requestingUser, cameraId, groupId);
        })
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> addUserToGroup(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");

    Mono<GroupData> groupUsersMono = groupService.findGroupDataById(groupId);

    Mono<UUID> userIdMono = request.bodyToMono(UserV1.class)
        .map(UserV1::getEmail)
        .flatMap(userService::findByEmail).map(User::getId);

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupUsersMono, userIdMono)
        .flatMap(tuple -> {
          UUID requestingUser = tuple.getT1();
          UUID newUser = tuple.getT3();
          if (!userInGroup(tuple.getT2(), requestingUser)) {
            throw new ForbiddenException("User not part of given group");
          }
          if (userInGroup(tuple.getT2(), newUser)) {
            throw new ConflictException("User already in group");
          }

          return groupService.addUserToGroup(tuple.getT3(), groupId);
        })
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> removeCameraFromGroup(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");
    UUID cameraId = getUUIDParameter(request, "cameraId");

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.findGroupDataById(groupId), cameraService.findById(cameraId))
        .flatMap(tuple -> {
          UUID requestorId = tuple.getT1();
          GroupData data = tuple.getT2();
          Camera selectedCamera = tuple.getT3()
              .orElseThrow(() -> new NotFoundException("Camera not found by given id"));

          if (requestorId.equals(data.getGroup().getOwnerId()) || selectedCamera.getOwnerId()
              .equals(requestorId)) {
            return groupService.removeCameraFromGroup(cameraId, groupId);
          }
          throw new ForbiddenException("User not admin or doesn't own selected camera");
        })
        .map(GroupMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getGroupCameras(ServerRequest request) {
    UUID groupId = getUUIDParameter(request, "groupId");

    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.findGroupDataById(groupId))
        .flatMapIterable(tuple2 -> {
          UUID requestorId = tuple2.getT1();
          GroupData data = tuple2.getT2();
          if (!data.getUserIds().contains(requestorId)) {
            throw new ForbiddenException("Must be in group to get cameras");
          }

          return data.getCameraIds();
        }).flatMap(cameraService::findById)
        .map(optionalCamera -> optionalCamera
            .orElseThrow(() -> new NotFoundException("Camera not found by given id")))
        .collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> deleteGroup(ServerRequest request) {
    LOGGER.info("Made it here");
    UUID groupId = getUUIDParameter(request, "groupId");
    return Mono.zip(
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        groupService.findGroupDataById(groupId))
        .flatMap(tuple2 -> {
          LOGGER.info("{} {}", tuple2.getT1(), tuple2.getT2().getGroup().getOwnerId());
          if (tuple2.getT1().equals(tuple2.getT2().getGroup().getOwnerId())) {
            return groupService.deleteGroup(groupId);
          } else {
            throw new VerificationException("User forbidden from deleteing group");
          }
        })
        .flatMap(data -> ServerResponse.status(HttpStatus.ACCEPTED).build())
        .onErrorResume(RouterUtil::handleErrors);
  }
}
