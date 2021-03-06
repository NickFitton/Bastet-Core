package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.entity.AccountType.BASIC;
import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.service.AccountService;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class LoginHandlerV1 {

  private final CameraService cameraService;
  private final UserService userService;
  private final AuthenticationService authenticationService;

  @Autowired
  LoginHandlerV1(
      CameraService cameraService,
      UserService userService,
      AuthenticationService authenticationService) {
    this.cameraService = cameraService;
    this.userService = userService;
    this.authenticationService = authenticationService;
  }

  public Mono<ServerResponse> cameraLogin(ServerRequest request) {
    return authorizationAndTokenGeneration(request, cameraService, CAMERA);
  }

  public Mono<ServerResponse> userLogin(ServerRequest request) {
    return authorizationAndTokenGeneration(request, userService, BASIC);
  }

  private <T> Mono<ServerResponse> authorizationAndTokenGeneration(
      ServerRequest request,
      AccountService<T, UUID> service,
      AccountType type) {
    return parseAuthorization(request, service, type)
        .flatMap(authenticationService::createAuthToken)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data))
        .onErrorResume(e -> ServerResponse.status(HttpStatus.FORBIDDEN)
            .syncBody(OutgoingDataV1.errorOnly(e.getMessage())));
  }

  /**
   * Returns to the requester their user data.
   *
   * @param request contains the session token of the requester to find data on.
   * @return HttpStatus.OK and a {@link com.nfitton.imagestorage.api.UserV1} related to the session
   *     token
   */
  public Mono<ServerResponse> getSelf(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userService::findById)
        .map(optionalUser -> optionalUser
            .orElseThrow(() -> new NotFoundException("User not found by given id")))
        .map(AccountMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data));
  }

  private <T> Mono<UUID> parseAuthorization(
      ServerRequest request, AccountService<T, UUID> service, AccountType type) {
    List<String> authorization = request.headers().header("authorization");
    if (authorization.size() != 1) {
      return Mono.error(new BadRequestException("login must have a single 'authorization' header"));
    }

    String[] s = authorization.get(0).split(" ");
    if (s.length != 2 || !s[0].equalsIgnoreCase("basic")) {
      return Mono.error(new BadRequestException(
          "Malformed authorization header, "
              + "should follow format: 'Basic {base64(username:password)}'"));
    }

    String credentials = new String(Base64.getDecoder().decode(s[1]));

    try {
      String accountId = credentials.split(":")[0];
      return service.authenticate(accountId, credentials.split(":")[1], type);
    } catch (IllegalArgumentException e) {
      return Mono.error(new BadRequestException(
          "Malformed account ID in authorization header, must be a valid UUID v4"));
    }
  }

}
