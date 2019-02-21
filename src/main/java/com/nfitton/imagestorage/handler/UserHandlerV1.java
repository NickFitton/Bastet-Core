package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.getUUIDParameter;

import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.UserGroup;
import com.nfitton.imagestorage.exception.ForbiddenException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.exception.VerificationException;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.mapper.GroupMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.GroupService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.util.UUID;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class UserHandlerV1 {

  private final PasswordEncoder encoder;
  private final UserService userService;
  private final Validator validator;
  private final AuthenticationService authenticationService;
  private final GroupService groupService;

  @Autowired
  public UserHandlerV1(
      Validator validator,
      PasswordEncoder encoder, UserService userService,
      AuthenticationService authenticationService,
      GroupService groupService) {
    this.validator = validator;
    this.encoder = encoder;
    this.userService = userService;
    this.authenticationService = authenticationService;
    this.groupService = groupService;
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserV1.class)
        .map((UserV1 v1) -> AccountMapper.newAccount(v1, encoder, validator))
        .flatMap(userService::save)
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.status(HttpStatus.CREATED).syncBody(account))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getUsers(ServerRequest request) {
    return userService.getAllIds().collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data));
  }

  public Mono<ServerResponse> getUser(ServerRequest request) {
    UUID userId = getUUIDParameter(request, "userId");

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .flatMap(authorizedUserId -> {
          if (authorizedUserId != null) {
            return userService.findById(userId);
          } else {
            return Mono
                .error(new VerificationException("Must be authorized to perform this request"));
          }
        })
        .map(account -> account.orElseThrow(
            () -> new NotFoundException(String.format("User not found by user ID: %s", userId))))
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.ok().syncBody(account))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> deleteUser(ServerRequest request) {
    UUID userId = getUUIDParameter(request, "userId");

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .flatMap(authorizedUserId -> {
          if (userId.equals(authorizedUserId)) {
            return Mono.just(true);
          }
          return userService.idIsAdmin(authorizedUserId);
        })
        .flatMap(isAuthenticated -> {
          if (isAuthenticated) {
            return userService.deleteById(userId);
          } else {
            return Mono.error(new VerificationException("Must be admin to perform this request"));
          }
        }).flatMap(deleted -> {
          if (deleted) {
            return ServerResponse.noContent().build();
          } else {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        })
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getGroupsForUser(ServerRequest request) {
    UUID userId = getUUIDParameter(request, "userId");

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .flatMapMany(authorizedUserId -> {
          if (userId.equals(authorizedUserId)) {
            return groupService.getGroupsByUserId(userId);
          }
          throw new ForbiddenException("Cannot request other users groups");
        })
        .map(UserGroup::getGroupId)
        .flatMap(groupService::findGroupDataById)
        .map(GroupMapper::toV1)
        .collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.OK).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }
}
