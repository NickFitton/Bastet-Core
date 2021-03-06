package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.parseAuthenticationToken;

import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.mapper.ImageMetadataMapper;
import com.nfitton.imagestorage.model.AnalysisQueueMessage;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.CameraService;
import com.nfitton.imagestorage.service.FileMetadataService;
import com.nfitton.imagestorage.service.FileUploadService;
import com.nfitton.imagestorage.service.UserService;
import com.nfitton.imagestorage.util.ExceptionUtil;
import com.nfitton.imagestorage.util.RouterUtil;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MotionHandlerV1 {

  private static final String MOTION_ID = "motionId";
  private static final Logger LOGGER = LoggerFactory.getLogger(MotionHandlerV1.class);

  private final AuthenticationService authenticationService;
  private final FileMetadataService fileMetadataService;
  private final FileUploadService fileUploadService;
  private final CameraService cameraService;
  private final UserService userService;
  private final JmsTemplate jmsTemplate;

  @Autowired
  MotionHandlerV1(
      AuthenticationService authenticationService,
      FileMetadataService fileMetadataService,
      FileUploadService fileUploadService,
      CameraService cameraService,
      UserService userService,
      JmsTemplate jmsTemplate) {
    this.authenticationService = authenticationService;
    this.fileMetadataService = fileMetadataService;
    this.fileUploadService = fileUploadService;
    this.cameraService = cameraService;
    this.userService = userService;
    this.jmsTemplate = jmsTemplate;
  }

  public Mono<ServerResponse> postMotion(ServerRequest request) {
    return Mono
        .zip(
            parseAuthenticationToken(request, authenticationService),
            request.bodyToMono(ImageMetadataV1.class))
        .map(tuple -> ImageMetadataMapper.newMetadata(tuple.getT2(), tuple.getT1()))
        .flatMap(fileMetadataService::save)
        .map(ImageMetadataMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(savedData -> ServerResponse.status(HttpStatus.CREATED).syncBody(savedData))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Attaches an image to a motion object for the server to analyse.
   *
   * @param request the {@link ServerRequest} containing the image, motion id and camera
   *     credentials
   * @return HttpStatus.ACCEPTED on success
   */
  public Mono<ServerResponse> patchMotionPicture(ServerRequest request) {
    UUID imageId = UUID.fromString(request.pathVariable(MOTION_ID));
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
          /*
           * NOTE: Previously these two items were also zipped with the image uploaded function,
           * however, when this occurred only the `uploadFile` function was execute
           */
          return Mono.zip(
              cameraService.imageTaken(tuple.getT1()),
              fileUploadService.uploadFile(file, imageId));
        }).flatMap(tuple2 -> {
          // LOGGER.debug("Sending to queue: {}", imageId);
          // jmsTemplate
          //     .convertAndSend("analysisQueue",
          // new AnalysisQueueMessage(tuple2.getT2(), imageId));
          return ServerResponse.accepted().build();
        })
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Returns data related to the given cameras.
   *
   * @param request the {@link ServerRequest} containing the user credentials and cameraId
   * @return HttpStatus.OK with a list of {@link ImageMetadataV1}
   */
  public Mono<ServerResponse> getMotion(ServerRequest request) {
    List<String> tags = getQueryTags(request);

    return parseAuthenticationToken(request, authenticationService)
        .flatMap(userService::existsById)
        .flatMapMany(exists -> {
          if (exists) {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime from = request.queryParam("from").map(ZonedDateTime::parse)
                .orElse(now.minusDays(7));
            ZonedDateTime to = request.queryParam("to").map(ZonedDateTime::parse).orElse(now);
            return Flux.fromStream(getCamerasParam(request))
                .flatMap(cameraId -> fileMetadataService.findAllByCameraId(cameraId, from, to));
          } else {
            return Mono.error(ExceptionUtil.badCredentials());
          }
        })
        .filter(imageData -> {
          if (tags.size() > 0) {
            return imageData
                .getEntities()
                .stream()
                .map(ImageEntity::getType)
                .anyMatch(tags::contains);
          }
          return true;
        })
        .map(ImageMetadataMapper::toV1)
        .collectList()
        .map(OutgoingDataV1::dataOnly)
        .flatMap(outgoingData -> ServerResponse.ok().syncBody(outgoingData))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Returns image metadata related to the given motionId.
   *
   * @param request the {@link ServerRequest} containing the user credentials and cameraId
   * @return HttpStatus.OK with a single {@link ImageMetadataV1}
   */
  public Mono<ServerResponse> getMotionById(ServerRequest request) {
    return parseAuthenticationToken(request, authenticationService)
        .flatMap(userService::existsById)
        .flatMap(exists -> {
          if (exists) {
            UUID motionId = RouterUtil.getUuidParameter(request, MOTION_ID);
            return fileMetadataService.findById(motionId);
          } else {
            return Mono.error(ExceptionUtil.badCredentials());
          }
        }).map(ImageMetadataMapper::toV1)
        .map(OutgoingDataV1::dataOnly)
        .flatMap(metadata -> ServerResponse.ok().syncBody(metadata))
        .onErrorResume(RouterUtil::handleErrors);
  }

  /**
   * Returns the image related to the given motionId.
   *
   * @param request the {@link ServerRequest} containing the motionId and user credentials
   * @return HttpStatus.OK with an image
   */
  public Mono<ServerResponse> getMotionImageById(ServerRequest request) {
    Mono<UUID> motionId = Mono.just(RouterUtil.getUuidParameter(request, MOTION_ID));

    Flux<byte[]> image = parseAuthenticationToken(request, authenticationService)
        .flatMap(userService::existsById).zipWith(motionId)
        .flatMap(tuple2 -> {
          if (tuple2.getT1()) {
            return fileMetadataService.findById(tuple2.getT2())
                .zipWith(Mono.just(tuple2.getT2()));
          } else {
            return Mono.error(ExceptionUtil.badCredentials());
          }
        })
        .flatMapMany(tuple2 -> {
          if (tuple2.getT1().fileExists()) {
            return fileUploadService.downloadFile(tuple2.getT2());
          }
          throw new NotFoundException("Motion does not have allocated image");
        });
    return ServerResponse.ok().contentType(MediaType.IMAGE_JPEG).body(image, byte[].class)
        .onErrorResume(RouterUtil::handleErrors);
  }

  private static Stream<UUID> getCamerasParam(ServerRequest request) {
    return request.queryParam("cameras")
        .map(param -> Arrays.asList(param.split(",")))
        .orElse(Collections.emptyList())
        .stream()
        .map(UUID::fromString);
  }

  private static List<String> getQueryTags(ServerRequest request) {
    return request
        .queryParam("tags")
        .map(tags -> tags.split(","))
        .map(Arrays::asList)
        .orElseGet(LinkedList::new);
  }
}
