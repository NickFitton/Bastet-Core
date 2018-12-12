package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.mapper.ImageMetadataMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.FileMetadataService;
import com.nfitton.imagestorage.service.FileUploadService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class MotionHandlerV1 {

  private final AuthenticationService authenticationService;
  private final FileMetadataService fileMetadataService;
  private final FileUploadService fileUploadService;
  private final CameraService cameraService;

  @Autowired
  MotionHandlerV1(
      AuthenticationService authenticationService,
      FileMetadataService fileMetadataService,
      FileUploadService fileUploadService,
      CameraService cameraService) {
    this.authenticationService = authenticationService;
    this.fileMetadataService = fileMetadataService;
    this.fileUploadService = fileUploadService;
    this.cameraService = cameraService;
  }

  public Mono<ServerResponse> postMotion(ServerRequest request) {
    Mono<ImageMetadataV1> savedData = Mono
        .zip(authenticatedCamera(request), request.bodyToMono(ImageMetadataV1.class))
        .map(tuple -> ImageMetadataMapper.newMetadata(tuple.getT2(), tuple.getT1()))
        .flatMap(fileMetadataService::save)
        .map(ImageMetadataMapper::toV1);
    return ServerResponse.status(HttpStatus.CREATED).body(savedData, ImageMetadataV1.class);
  }

  public Mono<ServerResponse> patchMotionPicture(ServerRequest request) {
    UUID imageId = UUID.fromString(request.pathVariable("motionId"));
    Mono<ImageMetadataV1> updatedMetadata = Mono
        .zip(authenticatedCamera(request), fileMetadataService.exists(imageId))
        .flatMap(tuple -> {
          if (!tuple.getT2()) {
            return Mono.error(new NotFoundException("Image does not exist by given image ID"));
          }
          return Mono.zip(Mono.just(tuple.getT1()), request.body(BodyExtractors.toMultipartData()));
        })
        .flatMap(tuple -> {
          FilePart file = (FilePart) tuple.getT2().toSingleValueMap().get("file");
          return Mono.zip(
              fileUploadService.uploadFile(file, imageId),
              fileMetadataService.imageUploaded(imageId), cameraService.imageTaken(tuple.getT1()));
        }).map(Tuple2::getT2)
        .map(ImageMetadataMapper::toV1);
    return ServerResponse.ok().body(updatedMetadata, ImageMetadataV1.class);
  }

  public Mono<ServerResponse> getMotion(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public Mono<ServerResponse> getMotionById(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public Mono<ServerResponse> getMotionImageById(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  private Mono<UUID> authenticatedCamera(ServerRequest request) {
    List<String> authentication = request.headers().header("authorization");
    if (authentication.size() != 1) {
      return Mono.error(new BadRequestException("login must have a single 'authorization' header"));
    }

    String token = authentication.get(0);
    String[] splitToken = token.split(" ");
    if (!splitToken[0].toLowerCase().equals("token")) {
      return Mono.error(new BadRequestException(
          "Malformed authorization header, should follow format: 'Token {token}'"));
    }

    return authenticationService.parseAuthentication(splitToken[1]);
  }
}
