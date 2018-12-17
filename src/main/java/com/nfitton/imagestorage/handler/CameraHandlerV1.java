package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.service.CameraService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class CameraHandlerV1 {

  private final PasswordEncoder encoder;
  private final CameraService service;

  @Autowired
  public CameraHandlerV1(PasswordEncoder encoder, CameraService service) {
    this.encoder = encoder;
    this.service = service;
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
        .map((CameraV1 v1) -> CameraMapper.toEntity(v1, encoder))
        .flatMap(service::save)
        .map(CameraMapper::toApiBean)
        .map(camera -> new OutgoingDataV1(camera, null))
        .flatMap(data -> ServerResponse.status(HttpStatus.CREATED).syncBody(data));
  }

  public Mono<ServerResponse> getCameras(ServerRequest request) {
    Mono<OutgoingDataV1> outgoingData = service.getAllIds().collectList()
        .map(cameraIds -> new OutgoingDataV1(cameraIds, null));

    return ServerResponse.ok().body(outgoingData, OutgoingDataV1.class);
  }

  public Mono<ServerResponse> getCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);
    return service.findById(cameraId).flatMap(optionalCamera -> {
      Optional<OutgoingDataV1> output = optionalCamera
          .map(CameraMapper::toApiBean)
          .map(cameraV1 -> new OutgoingDataV1(cameraV1, null));
      if (output.isPresent()) {
        return ServerResponse.ok().syncBody(output.get());
      }
      return ServerResponse.notFound().build();
    });
  }

  public Mono<ServerResponse> deleteCamera(ServerRequest request) {
    UUID cameraId = getCameraId(request);

    return service.deleteById(cameraId).then(ServerResponse.noContent().build());
  }
}
