package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
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
      AuthenticationService authenticationService) {
    this.validator = validator;
    this.encoder = encoder;
    this.cameraService = cameraService;
    this.authenticationService = authenticationService;
  }

  private static Mono<UUID> getCameraId(ServerRequest request) {
    try {
      UUID cameraId = UUID.fromString(request.pathVariable("cameraId"));
      return Mono.just(cameraId);
    } catch (IllegalArgumentException e) {
      return Mono.error(new BadRequestException("Camera ID must be a valid UUID v4"));
    }
  }

  /**
   * Create a new camera given a password, used by the camera when it starts up.
   *
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
        .doOnSuccess(uuid -> LOGGER.debug("User {} retrieving their camera info", uuid))
        .flatMapMany(cameraService::findAllOwnedById)
        .map(CameraMapper::toApiBean)
        .collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Retrieves data about a camera if the requester owns the camera queried.
   *
   * @param request the {@link ServerRequest} containing the camera id and user credentials
   * @return HttpStatus.OK and {@link CameraV1} on success
   */
  public Mono<ServerResponse> getCamera(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .zipWith(getCameraId(request))
        .flatMap(tuple2 -> cameraService.findById(tuple2.getT2()))
        .flatMap(optionalCamera -> {
          Optional<OutgoingDataV1> output = optionalCamera.map(CameraMapper::toApiBean)
              .map(OutgoingDataV1::dataOnly);
          return output.map(outgoingDataV1 -> ServerResponse.ok().syncBody(outgoingDataV1))
              .orElseGet(() -> ServerResponse.notFound().build());
        })
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Assigns a camera to the requesting user if the camera exists and hasn't already been assigned.
   *
   * @param request the {@link ServerRequest} containing the camera id and user credentials
   * @return HttpStatus.ACCEPTED on success
   */
  public Mono<ServerResponse> claimCamera(ServerRequest request) {
    Mono<Camera> cameraName = request.bodyToMono(CameraV1.class).map(CameraMapper::toEntity)
        .switchIfEmpty(Mono.just(new Camera()));

    return Mono.zip(
        cameraName,
        RouterUtil.parseAuthenticationToken(request, authenticationService),
        getCameraId(request))
        .flatMap(tuple3 -> {
          LOGGER.debug("Camera {} claimed by user {}", tuple3.getT3(), tuple3.getT2());
          return cameraService.updateCamera(tuple3.getT3(), tuple3.getT2(), tuple3.getT1());
        })
        .flatMap(camera -> ServerResponse.accepted().build());
  }

  public Mono<ServerResponse> deleteCamera(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .zipWith(getCameraId(request))
        .flatMap(tuple2 -> cameraService.deleteById(tuple2.getT2()))
        .then(ServerResponse.noContent().build())
        .onErrorResume(RouterUtil::handleErrors);
  }
}
