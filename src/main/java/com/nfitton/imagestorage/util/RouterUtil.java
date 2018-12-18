package com.nfitton.imagestorage.util;

import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.ConflictException;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.exception.OversizeException;
import com.nfitton.imagestorage.exception.ValidationException;
import com.nfitton.imagestorage.exception.VerificationException;
import com.nfitton.imagestorage.service.AuthenticationService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;
import reactor.core.publisher.Mono;

public class RouterUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterUtil.class);

  public static UUID getUUIDParameter(ServerRequest request, String name) {
    try {
      return UUID.fromString(request.pathVariable(name));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(
          String.format("Given parameter %s is not a valid UUID v4", name));
    }
  }

  public static Mono<UUID> parseAuthenticationToken(
      ServerRequest request,
      AuthenticationService service) {
    List<String> authentication = request.headers().header("authorization");
    if (authentication.size() != 1) {
      return Mono
          .error(new BadRequestException("Request must have a single 'authorization' header"));
    }

    String token = authentication.get(0);
    String[] splitToken = token.split(" ");
    if (!splitToken[0].toLowerCase().equals("token")) {
      return Mono.error(new BadRequestException(
          "Malformed authorization header, should follow format: 'Token {token}'"));
    }

    return service.parseAuthentication(splitToken[1]);

  }

  public static Mono<ServerResponse> handleErrors(Throwable e) {
    BodyBuilder status;

    if (e instanceof BadRequestException) {
      status = ServerResponse.badRequest();
    } else if (e instanceof NotFoundException) {
      status = ServerResponse.status(HttpStatus.NOT_FOUND);
    } else if (e instanceof VerificationException) {
      status = ServerResponse.status(HttpStatus.FORBIDDEN);
    } else if (e instanceof ConflictException) {
      status = ServerResponse.status(HttpStatus.CONFLICT);
    } else if (e instanceof OversizeException) {
      status = ServerResponse.status(HttpStatus.PAYLOAD_TOO_LARGE);
    } else if (e instanceof InternalServerException) {
      status = ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
    } else if (e instanceof ValidationException) {
      ValidationException exception = (ValidationException) e;
      return ServerResponse.badRequest()
          .syncBody(new OutgoingDataV1(null, exception.getViolations()));
    } else {
      LOGGER.error("Could not consume exception", e);
      return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return status.syncBody(OutgoingDataV1.errorOnly(e.getMessage()));
  }
}
