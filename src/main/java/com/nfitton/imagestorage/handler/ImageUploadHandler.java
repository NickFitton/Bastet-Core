package com.nfitton.imagestorage.handler;

import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.mapper.ImageMetadataMapper;
import com.nfitton.imagestorage.service.metadata.FileMetadataService;
import com.nfitton.imagestorage.service.upload.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class ImageUploadHandler {

  private final FileUploadService fileUploadService;
  private final FileMetadataService fileMetadataService;

  @Autowired public ImageUploadHandler(
      FileUploadService fileUploadService, FileMetadataService fileMetadataService) {
    this.fileUploadService = fileUploadService;
    this.fileMetadataService = fileMetadataService;
  }

  public Mono<ServerResponse> uploadFile(ServerRequest request) {
    UUID imageId = getImageId(request);

    return fileMetadataService
        .exists(imageId)
        .map(metadataExists -> {
          if (!metadataExists) {
            throw new BadRequestException("Metadata must exist for given id: " + imageId);
          } else {
            return true;
          }
        })
        .then(request.body(BodyExtractors.toMultipartData()))
        .map(MultiValueMap::toSingleValueMap)
        .map(map -> (FilePart) map.get("file"))
        .flatMap(filePart -> fileUploadService.uploadFile(filePart, imageId))
        .then(fileMetadataService.imageUploaded(imageId))
        .then(ServerResponse.accepted().build());
  }

  public Mono<ServerResponse> getFile(ServerRequest request) {
    UUID imageId = getImageId(request);

    Flux<byte[]> image = fileMetadataService
        .exists(imageId)
        .filter(exists -> exists)
        .flatMapMany(exists -> fileUploadService.downloadFile(imageId))
        .switchIfEmpty(Flux.empty());

    return ServerResponse.ok().contentType(MediaType.IMAGE_JPEG).body(image, byte[].class);
  }

  public Mono<ServerResponse> deleteFile(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  public Mono<ServerResponse> uploadMetadata(ServerRequest request) {
    return request
        .bodyToMono(ImageMetadataV1.class)
        .map(ImageMetadataMapper::newMetadata)
        .flatMap(fileMetadataService::save)
        .map(ImageMetadataMapper::toV1)
        .flatMap(uploadedMetadata -> ServerResponse.ok().syncBody(uploadedMetadata));
  }

  public Mono<ServerResponse> getMetadataInTimeframe(ServerRequest request) {
    ZonedDateTime from = request
        .queryParam("from")
        .map(ZonedDateTime::parse)
        .orElse(ZonedDateTime.now().minusDays(1));
    ZonedDateTime to =
        request.queryParam("to").map(ZonedDateTime::parse).orElse(ZonedDateTime.now());

    Flux<ImageMetadataV1> allExistedAt =
        fileMetadataService.findAllExistedAt(from, to).map(ImageMetadataMapper::toV1);
    return ServerResponse.ok().body(allExistedAt, ImageMetadataV1.class);
  }

  public Mono<ServerResponse> getMetadata(ServerRequest request) {
    UUID imageId = getImageId(request);

    return fileMetadataService
        .findById(imageId)
        .map(ImageMetadataMapper::toV1)
        .flatMap(metadata -> ServerResponse.ok().syncBody(metadata));
  }

  public Mono<ServerResponse> deleteFileData(ServerRequest request) {
    return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  private UUID getImageId(ServerRequest request) {
    try {
      return UUID.fromString(request.pathVariable("imageId"));
    } catch (Exception e) {
      throw new BadRequestException("Given image id was not a valid UUID");
    }
  }
}
