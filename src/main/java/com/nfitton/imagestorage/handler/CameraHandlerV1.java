package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.util.Optional;
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
public class CameraHandlerV1 {

  private final Validator validator;
  private final PasswordEncoder encoder;
  private final CameraService cameraService;
  private final AuthenticationService authenticationService;

  /**
   * Constructor that is used by spring to connect services to endpoints.
   */
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

  private static UUID getCameraId(ServerRequest request) {
    try {
      return UUID.fromString(request.pathVariable("cameraId"));
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Camera ID must be a valid UUID v4");
    }
  }

  /**
   * Saves the given camera if the camera is valid.
   *
   * @param request the incoming request
   * @return the saved {@link CameraV1} wrapped in {@link ServerResponse} with a 201 on success
   */
  public Mono<ServerResponse> postCamera(ServerRequest request) {
    return request.bodyToMono(CameraV1.class)
        .map((CameraV1 v1) -> CameraMapper.toEntity(v1, encoder, validator))
        .flatMap(cameraService::save)
        .map(CameraMapper::toApiBean)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.status(HttpStatus.CREATED).syncBody(data))
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }

  /**
   * Retrieves a list of all of the saved cameras {@link UUID} id.
   *
   * @param request the incoming request
   * @return an array of {@link UUID} ids of cameras saved in the system
   */
  public Mono<ServerResponse> getCameras(ServerRequest request) {
    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMapMany(userId -> cameraService.getAllIds())
        .collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(data -> ServerResponse.ok().syncBody(data))
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }

  /**
   * Retrieves camera information based on the given {@link UUID} id.
   *
   * @param request the incoming request
   * @return a {@link CameraV1} related to the given {@link UUID} id.
   */
  public Mono<ServerResponse> getCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userId -> cameraService.findById(cameraId))
        .flatMap(optionalCamera -> {
          Optional<OutgoingDataV1> output = optionalCamera
              .map(CameraMapper::toApiBean)
              .map(OutgoingDataV1::dataOnly);
          if (output.isPresent()) {
            return ServerResponse.ok().syncBody(output.get());
          }
          return ServerResponse.notFound().build();
        })
        .onErrorResume(RouterUtil::handleErrors)
        .subscribeOn(Schedulers.elastic());
  }

  /**
   * Deletes the camera related to the given {@link UUID id}.
   *
   * @param request the incoming request
   * @return {@link HttpStatus} of no content (204) if deleted successfully
   */
  public Mono<ServerResponse> deleteCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return RouterUtil.parseAuthenticationToken(request, authenticationService)
        .flatMap(userId -> cameraService.deleteById(cameraId))
        .then(ServerResponse.noContent().build())
        .onErrorResume(RouterUtil::handleErrors);
  }
}
