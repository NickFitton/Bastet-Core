package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.api.KeyExchangeV1;
import com.nfitton.imagestorage.mapper.CameraMapper;
import com.nfitton.imagestorage.service.CameraService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class CameraHandler {

  private CameraService service;

  @Autowired public CameraHandler(CameraService service) {
    this.service = service;
  }

  public Mono<ServerResponse> register(ServerRequest request) {
    return request
        .bodyToMono(CameraV1.class)
        .map(CameraMapper::toEntity)
        .flatMap(service::register)
        .map(CameraMapper::toApiBean)
        .flatMap(bean -> ServerResponse.status(HttpStatus.CREATED).syncBody(bean));
  }

  public Mono<ServerResponse> getAll(ServerRequest request) {
    return ServerResponse.ok().body(service.getAll().map(CameraMapper::toApiBean), CameraV1.class);
  }

  public Mono<ServerResponse> handshake(ServerRequest request) {
    try {
      UUID cameraId = UUID.fromString(request.pathVariable("cameraId"));
      return request
          .bodyToMono(KeyExchangeV1.class)
          .map(KeyExchangeV1::getPublicKey)
          .flatMap(devicePublicKey -> service.startHandshake(cameraId, devicePublicKey))
          .map(KeyExchangeV1::new)
          .flatMap(keyExchangeV1 -> ServerResponse.ok().syncBody(keyExchangeV1));
    } catch (IllegalArgumentException e) {
      return ServerResponse.badRequest().syncBody("Given cameraId was not a valid UUID");
    }
  }
}
