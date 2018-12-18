package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.parseAuthenticationToken;

import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.configuration.ApiConfiguration;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.OversizeException;
import com.nfitton.imagestorage.exception.VerificationException;
import com.nfitton.imagestorage.mapper.ImageMetadataMapper;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.FileMetadataService;
import com.nfitton.imagestorage.service.FileUploadService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.RouterUtil;
import java.time.ZonedDateTime;
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
  private final UserService userService;
  private final ApiConfiguration apiConfiguration;

  @Autowired
  MotionHandlerV1(
      AuthenticationService authenticationService,
      FileMetadataService fileMetadataService,
      FileUploadService fileUploadService,
      CameraService cameraService,
      UserService userService,
      ApiConfiguration apiConfiguration) {
    this.authenticationService = authenticationService;
    this.fileMetadataService = fileMetadataService;
    this.fileUploadService = fileUploadService;
    this.cameraService = cameraService;
    this.userService = userService;
    this.apiConfiguration = apiConfiguration;
  }

  public Mono<ServerResponse> postMotion(ServerRequest request) {
    Mono<ImageMetadataV1> savedData = Mono
        .zip(
            parseAuthenticationToken(request, authenticationService),
            request.bodyToMono(ImageMetadataV1.class))
        .map(tuple -> ImageMetadataMapper.newMetadata(tuple.getT2(), tuple.getT1()))
        .flatMap(fileMetadataService::save)
        .map(ImageMetadataMapper::toV1);
    return ServerResponse.status(HttpStatus.CREATED).body(savedData, ImageMetadataV1.class);
  }

  public Mono<ServerResponse> patchMotionPicture(ServerRequest request) {
    UUID imageId = UUID.fromString(request.pathVariable("motionId"));
    return Mono.zip(
        parseAuthenticationToken(request, authenticationService),
        fileMetadataService.findById(imageId))
        .flatMap(tuple -> {
          if (!tuple.getT2().getCameraId().equals(tuple.getT1())) {
            return Mono
                .error(new BadRequestException("Image was not uploaded by metadata creator"));
          }
          return Mono.zip(Mono.just(tuple.getT1()), request.body(BodyExtractors.toMultipartData()));
        }).flatMap(tuple -> {
          FilePart file = (FilePart) tuple.getT2().toSingleValueMap().get("file");
          return Mono.zip(
              fileUploadService.uploadFile(file, imageId),
              fileMetadataService.imageUploaded(imageId), cameraService.imageTaken(tuple.getT1()));
        })
        .map(Tuple2::getT2)
        .map(ImageMetadataMapper::toV1)
        .flatMap(metadata -> ServerResponse.ok().syncBody(metadata))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getMotion(ServerRequest request) {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime from = request.queryParam("from").map(ZonedDateTime::parse)
        .orElse(now.minusDays(7));
    ZonedDateTime to = request.queryParam("to").map(ZonedDateTime::parse).orElse(now);

    return parseAuthenticationToken(request, authenticationService)
        .flatMap(userService::existsById)
        .flatMapMany(exists -> {
          if (exists) {
            return fileMetadataService.findAllExistedAt(from, to);
          } else {
            return Mono.error(new VerificationException());
          }
        }).map(ImageMetadataMapper::toV1)
        .collectList()
        .map(data -> {
          int max = apiConfiguration.getMaxPayload();
          if (data.size() > max) {
            throw new OversizeException(max, data.size());
          }
          return OutgoingDataV1.dataOnly(data);
        })
        .flatMap(outgoingData -> ServerResponse.ok().syncBody(outgoingData))
        .onErrorResume(RouterUtil::handleErrors);
  }

  public Mono<ServerResponse> getMotionById(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public Mono<ServerResponse> getMotionImageById(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
