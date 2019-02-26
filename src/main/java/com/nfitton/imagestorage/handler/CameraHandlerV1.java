package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.entity.Camera;
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
    this.authenticationService = authenticationService;
  }

  private static UUID getCameraId(ServerRequest request) {
    try {
      return UUID.fromString(request.pathVariable("cameraId"));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Camera ID must be a valid UUID v4");
    }
  }

  /**
   * Create a new camera given a password, used by the camera when it starts up.
   * @param request the {@link ServerRequest} containing the cameras password
   * @return HttpStatus.CREATED and the created {@link CameraV1} on success
   */
  public Mono<ServerResponse> postCamera(ServerRequest request) {
    LOGGER.debug("Creating camera");
    return request.bodyToMono(CameraV1.class)
        .map((CameraV1 v1) -> CameraMapper.toEntity(v1, encoder, validator))
        .flatMap(cameraService::save)
        .map(camera -> {
          LOGGER.debug("Camera created with id: {}", camera.getId());
          return CameraMapper.toApiBean(camera);
        })
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.CREATED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getCameras(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMapMany(cameraService::findAllOwnedById)
        .map(CameraMapper::toApiBean)
        .collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Retrieves data about a camera if the requester owns the camera queried.
   * @param request the {@link ServerRequest} containing the camera id and user credentials
   * @return HttpStatus.OK and {@link CameraV1} on success
   */
  public Mono<ServerResponse> getCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userId -> cameraService.findById(cameraId))
        .flatMap(optionalCamera -> {
          Optional<OutgoingDataV1> output = optionalCamera.map(CameraMapper::toApiBean)
              .map(OutgoingDataV1::dataOnly);
          if (output.isPresent()) {
            return ServerResponse.ok().syncBody(output.get());
          }
          return ServerResponse.notFound().build();
        })
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Assigns a camera to the requesting user if the camera exists and hasn't already been assigned.
   * @param request the {@link ServerRequest} containing the camera id and user credentials
   * @return HttpStatus.ACCEPTED on success
   */
  public Mono<ServerResponse> claimCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    Mono<Camera> cameraName = request.bodyToMono(CameraV1.class).map(CameraMapper::toEntity)
        .switchIfEmpty(Mono.just(new Camera()));

    return Mono.zip(
        cameraName,
        RouterUtil.parseAuthenticationToken(request, authenticationService))
        .flatMap(tuple2 -> {
          LOGGER.debug("Camera {} updated by user", cameraId, tuple2.getT2());
          return cameraService.updateCamera(cameraId, tuple2.getT2(), tuple2.getT1());
        })
        .flatMap(camera -> ServerResponse.accepted().build());
  }

  public Mono<ServerResponse> deleteCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userId -> cameraService.deleteById(cameraId))
        .then(ServerResponse.noContent().build())
        .onErrorResume(RouterUtil::handleErrors);
  }
}
