package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.util.Optional;
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
public class CameraHandlerV1 {

  private static final Logger LOGGER = LoggerFactory.getLogger(CameraHandlerV1.class);

  private final Validator validator;
  private final PasswordEncoder encoder;
  private final CameraService cameraService;
  private final UserService userService;
  private final AuthenticationService authenticationService;

  @Autowired
  public CameraHandlerV1(
      Validator validator,
      PasswordEncoder encoder,
      CameraService cameraService,
      UserService userService,
      AuthenticationService authenticationService) {
    this.validator = validator;
    this.encoder = encoder;
    this.cameraService = cameraService;
    this.userService = userService;
    this.authenticationService = authenticationService;
  }

  private static UUID getCameraId(ServerRequest request) {
    try {
      return UUID.fromString(request.pathVariable("cameraId"));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Camera ID must be a valid UUID v4");
    }
  }

  public Mono<ServerResponse> postCamera(ServerRequest request) {
    return request.bodyToMono(CameraV1.class)
        .map((CameraV1 v1) -> CameraMapper.toEntity(v1, encoder, validator))
        .flatMap(cameraService::save)
        .map(CameraMapper::toApiBean)
        .map(camera -> new OutgoingDataV1(camera, null))
        .flatMap(data -> ServerResponse.status(HttpStatus.CREATED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getCameras(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userId -> {
          LOGGER.info(
              String.format("User %s retreiving camera id list", userId));
          Mono<OutgoingDataV1> outgoingData = cameraService.getAllIds().collectList()
              .map(cameraIds -> new OutgoingDataV1(cameraIds, null));

          return ServerResponse.ok().body(outgoingData, OutgoingDataV1.class);
        })
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userId -> {
          LOGGER.info(
              String.format("User %s retreiving information for camera %s", userId, cameraId));

          return cameraService.findById(cameraId);
        }).flatMap(optionalCamera -> {
          Optional<OutgoingDataV1> output = optionalCamera
              .map(CameraMapper::toApiBean)
              .map(cameraV1 -> new OutgoingDataV1(cameraV1, null));
          if (output.isPresent()) {
            return ServerResponse.ok().syncBody(output.get());
          }
          return ServerResponse.notFound().build();
        }).onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> deleteCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return cameraService
        .deleteById(cameraId)
        .then(ServerResponse.noContent().build())
        .onErrorResume(RouterUtil::handleErrors);
  }
}
