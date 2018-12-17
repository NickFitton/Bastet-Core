package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.entity.AccountType.BASIC;
import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.service.AccountService;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.UserService;
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
        .flatMap(token -> ServerResponse.ok().syncBody(new OutgoingDataV1(token, null)))
        .onErrorResume(e -> ServerResponse.status(HttpStatus.FORBIDDEN)
            .syncBody(new OutgoingDataV1(null, e.getMessage())));
  }

  private <T> Mono<UUID> parseAuthorization(
      ServerRequest request,
      AccountService<T, UUID> service,
      AccountType type) throws BadRequestException {
    List<String> authorization = request.headers().header("authorization");
    if (authorization.size() != 1) {
      return Mono.error(new BadRequestException("login must have a single 'authorization' header"));
    }

    String[] s = authorization.get(0).split(" ");
    if (s.length != 2 || !s[0].toLowerCase().equals("basic")) {
      return Mono.error(new BadRequestException(
          "Malformed authorization header, should follow format: 'Basic {base64(username:password)}'"));
    }

    String credentials = new String(Base64.getDecoder().decode(s[1]));

    try {
      String accountId = credentials.split(":")[0];
      return service.authenticate(accountId, credentials.split(":")[1], type);
    } catch (IllegalArgumentException e) {
      return Mono.error(new BadRequestException(
          "Malformed camera ID in authorization header, must be a valid UUID v4"));
    }
  }

}
