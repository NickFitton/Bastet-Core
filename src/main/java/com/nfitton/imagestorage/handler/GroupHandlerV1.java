package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.getUUIDParameter;

import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.exception.ForbiddenException;
import com.nfitton.imagestorage.mapper.GroupMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.GroupService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.util.UUID;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class GroupHandlerV1 {

  private final GroupService groupService;
  private final AuthenticationService authenticationService;
  private final Validator validator;

  @Autowired
  public GroupHandlerV1(
      GroupService groupService,
      AuthenticationService authenticationService,
      Validator validator) {
    this.groupService = groupService;
    this.authenticationService = authenticationService;
    this.validator = validator;
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
        .flatMap(account -> ServerResponse.status(HttpStatus.CREATED).syncBody(account))
        .onErrorResume(RouterUtil::handleErrors);
  }
}
