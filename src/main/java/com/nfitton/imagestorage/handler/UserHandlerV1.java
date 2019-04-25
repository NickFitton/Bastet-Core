package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.ExceptionUtil.userNotFound;
import static com.nfitton.imagestorage.util.RouterUtil.getUuidParameter;

import com.nfitton.imagestorage.api.GroupV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.PasswordV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.entity.User.Builder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class UserHandlerV1 {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserHandlerV1.class);
  private static final String USER_ID = "userId";

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
    LOGGER.debug("Creating user");
    return request.bodyToMono(UserV1.class)
        .map((UserV1 v1) -> AccountMapper.newAccount(v1, encoder, validator))
        .flatMap(userService::save)
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.status(HttpStatus.CREATED).syncBody(account))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getUsers(ServerRequest request) {
    LOGGER.debug("Retrieving user list");
    return userService.getAllIds().collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Returns a user by the given userId.
   *
   * @param request the {@link ServerRequest} containing the userId and requesting users
   *     credentials
   * @return HttpStatus.OK and {@link UserV1} related to the given userId
   */
  public Mono<ServerResponse> getUser(ServerRequest request) {
    LOGGER.debug("Retrieving user information");
    UUID userId = getUuidParameter(request, USER_ID);

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .flatMap(authorizedUserId -> {
          if (authorizedUserId != null) {
            if (!userId.equals(authorizedUserId)) {
              LOGGER.debug("{} is retrieving {} data", authorizedUserId, userId);
            } else {
              LOGGER.debug("{} is retrieving their data", authorizedUserId);
            }
            return userService.findById(userId);
          } else {
            LOGGER.debug("User not verified");
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

  /**
   * Updates a users key information including name and email address.
   *
   * @param request the {@link ServerRequest} containing the userId and requesting users
   *     credentials
   * @return HttpStatus.ACCEPTED on success
   */
  public Mono<ServerResponse> updateUser(ServerRequest request) {
    LOGGER.debug("Updating user information");
    UUID userId = getUuidParameter(request, USER_ID);

    return getAuthenticatedUserIsChanging(request)
        .zipWith(request.bodyToMono(UserV1.class))
        .flatMap(tuple2 -> {
          User existingDetails = tuple2.getT1();
          UserV1 newDetails = tuple2.getT2();
          Builder updatedUserDetails = User.Builder.clone(existingDetails);
          if (newDetails.getFirstName() != null) {
            updatedUserDetails.withFirstName(newDetails.getFirstName());
          }
          if (newDetails.getLastName() != null) {
            updatedUserDetails.withLastName(newDetails.getLastName());
          }
          if (newDetails.getEmail() != null) {
            updatedUserDetails.withEmail(newDetails.getEmail());
          }
          return userService.save(updatedUserDetails.build());
        })
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(updatedUser -> ServerResponse.accepted().syncBody(updatedUser))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Updates a users password once the current password is validated.
   *
   * @param request the {@link ServerRequest} containing the userId and requesting users
   *     credentials
   * @return HttpStatus.ACCEPTED on success
   */
  public Mono<ServerResponse> updateUserPassword(ServerRequest request) {
    LOGGER.debug("Updating user password");

    return getAuthenticatedUserIsChanging(request)
        .zipWith(request.bodyToMono(PasswordV1.class))
        .flatMap(tuple2 -> {
          User existingDetails = tuple2.getT1();
          if (!encoder
              .matches(tuple2.getT2().getCurrentPassword(), existingDetails.getPassword())) {
            throw new ForbiddenException("Given password is incorrect");
          } else if (encoder
              .matches(tuple2.getT2().getNewPassword(), existingDetails.getPassword())) {
            throw new ForbiddenException("Password has not changed");
          }
          String newEncodedPassword = encoder.encode(tuple2.getT2().getNewPassword());
          User updatedDetails = User.Builder.clone(existingDetails).withPassword(newEncodedPassword)
              .build();
          return userService.save(updatedDetails);
        })
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(updatedUser -> ServerResponse.accepted().syncBody(updatedUser))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Removes a user from the platform if the user to delete is the requester.
   *
   * @param request the {@link ServerRequest} containing the userId and requesting users
   *     credentials
   * @return HttpStatus.NO_CONTENT on success
   */
  public Mono<ServerResponse> deleteUser(ServerRequest request) {
    LOGGER.debug("Deleting user");
    UUID userId = getUuidParameter(request, USER_ID);

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .flatMap(authorizedUserId -> {
          if (userId.equals(authorizedUserId)) {
            LOGGER.debug("{} is deleting their own account", authorizedUserId, userId);
            return userService.deleteById(userId);
          } else {
            LOGGER.warn(
                "Unauthorized user {} attempted to delete account {}", authorizedUserId, userId);
            return Mono.error(new ForbiddenException("Cannot delete other users account"));
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

  /**
   * Returns a list of groups the given user is associated with.
   *
   * @param request the {@link ServerRequest} containing the userId and requesting users
   *     credentials
   * @return a Flux stream of {@link GroupV1} related to the given userId
   */
  public Mono<ServerResponse> getGroupsForUser(ServerRequest request) {
    LOGGER.debug("Retreiving user groups");
    UUID userId = getUuidParameter(request, USER_ID);

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .zipWith(Mono.just(userId))
        .flatMapMany(tuple2 -> {
          if (tuple2.getT2().equals(tuple2.getT1())) {
            LOGGER.info("User {} retrieving their group data", tuple2.getT1());
            return groupService.getGroupsByUserId(tuple2.getT1());
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

  private Mono<User> getAuthenticatedUserIsChanging(ServerRequest request) {
    UUID userId = getUuidParameter(request, USER_ID);

    return RouterUtil
        .parseAuthenticationToken(request, authenticationService)
        .flatMap(authorizedUserId -> {
          if (authorizedUserId != null && authorizedUserId.equals(userId)) {
            return userService.findById(userId);
          } else {
            LOGGER.debug("User {} not verified to update {}'s information",
                         authorizedUserId, userId);
            return Mono.error(
                new VerificationException("Must be authorized to perform this request"));
          }
        })
        .map(optionalUser -> {
          if (!optionalUser.isPresent()) {
            throw userNotFound();
          } else {
            return optionalUser.get();
          }
        });
  }
}
