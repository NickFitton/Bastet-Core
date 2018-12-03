package com.nfitton.imagestorage.service.metadata;

import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.model.TallyPoint;
import com.nfitton.imagestorage.model.TimeFrame;

import java.time.ZonedDateTime;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileMetadataService {

  Mono<ImageMetadata> save(ImageMetadata metadata);

  Mono<ImageMetadata> imageUploaded(UUID imageId);

  Mono<Boolean> exists(UUID imageId);

  Mono<ImageMetadata> findById(UUID metadataId);

  Flux<ImageMetadata> findAllExistedAt(ZonedDateTime time);

  Flux<ImageMetadata> findAllExistedAt(ZonedDateTime from, ZonedDateTime to);

  Flux<TallyPoint> countAllExistedAt(ZonedDateTime start, ZonedDateTime end, TimeFrame measurement);

  Flux<ImageMetadata> findAll();
}
