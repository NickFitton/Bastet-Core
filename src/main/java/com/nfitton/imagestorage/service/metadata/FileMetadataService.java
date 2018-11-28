package com.nfitton.imagestorage.service.metadata;

import com.nfitton.imagestorage.entity.ImageMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface FileMetadataService {

  Mono<ImageMetadata> save(ImageMetadata metadata);

  Mono<ImageMetadata> imageUploaded(UUID imageId);

  Mono<Boolean> exists(UUID imageId);

  Mono<ImageMetadata> findById(UUID metadataId);

  Flux<ImageMetadata> findAllExistedAt(ZonedDateTime time);

  Flux<ImageMetadata> findAllExistedAt(ZonedDateTime from, ZonedDateTime to);

  Flux<ImageMetadata> findAll();
}
