package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.getUUIDParameter;

import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.api.UserV1;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.exception.VerificationException;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
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
import reactor.core.scheduler.Schedulers;

@Component
public class UserHandlerV1 {

  private final PasswordEncoder encoder;
  private final UserService userService;
  private final Validator validator;
  private final AuthenticationService authenticationService;

  /**
   * Constructor that is used by spring to connect services to endpoints.
   */
  @Autowired
  public UserHandlerV1(
      Validator validator,
      PasswordEncoder encoder, UserService userService,
      AuthenticationService authenticationService) {
    this.validator = validator;
    this.encoder = encoder;
    this.userService = userService;
    this.authenticationService = authenticationService;
  }

  /**
   * Saves a new user if the user information is valid.
   *
   * @param request the incoming request
   * @return the saved {@link UserV1} and a status of CREATED (201)
   */
  public Mono<ServerResponse> createUser(ServerRequest request) {
    return request.bodyToMono(UserV1.class)
        .map((UserV1 v1) -> AccountMapper.newAccount(v1, encoder, validator))
        .flatMap(userService::save)
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.status(HttpStatus.CREATED).syncBody(account))
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }

  /**
   * Retrieves a list of all the users {@link UUID} ids as long as the requestor is an ADMIN type.
   *
   * @param request the incoming request
   * @return A list of user {@link UUID} id
   */
  public Mono<ServerResponse> getUsers(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userService::idIsAdmin)
        .flatMap(isAdmin -> {
          if (!isAdmin) {
            return Mono.error(new VerificationException());
          }
          return userService.getAllIds().collectList();
        })
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data))
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }

  /**
   * Returns the details of a user related to the user {@link UUID} id from the request.
   *
   * @param request the incoming request
   * @return a {@link UserV1} with a status of OK (200)
   */
  public Mono<ServerResponse> getUser(ServerRequest request) {
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
            return userService.findById(userId);
          } else {
            return Mono.error(new VerificationException());
          }
        })
        .map(account -> account.orElseThrow(
            () -> new NotFoundException(String.format("User not found by user ID: %s", userId))))
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(account -> ServerResponse.ok().syncBody(account))
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }

  /**
   * Deletes the details of a user related to the user {@link UUID} id from the request.
   *
   * @param request the incoming request
   * @return an empty response with status NO_CONTENT (204)
   */
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
            return Mono.error(new VerificationException());
          }
        }).flatMap(deleted -> {
          if (deleted) {
            return ServerResponse.noContent().build();
          } else {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        })
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }
}
