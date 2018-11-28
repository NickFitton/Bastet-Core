package com.nfitton.imagestorage.service.metadata.impl;

import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.repository.FileMetadataRepository;
import com.nfitton.imagestorage.service.metadata.FileMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class MetadataServiceDatabaseImpl implements FileMetadataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataServiceDatabaseImpl.class);

  private final FileMetadataRepository repository;

  @Autowired public MetadataServiceDatabaseImpl(FileMetadataRepository repository) {
    this.repository = repository;
  }

  @Override public Mono<ImageMetadata> save(ImageMetadata metadata) {
    LOGGER.info("Saving metadata for id: {}", metadata.getId());
    return Mono.fromCallable(() -> repository.save(metadata));
  }

  @Override public Mono<ImageMetadata> imageUploaded(UUID imageId) {
    LOGGER.info("Updating image existence for id: {}", imageId);
    return findById(imageId)
        .map(ImageMetadata.Builder::clone)
        .map(builder -> builder.withFileExists(true).withUpdatedAt(ZonedDateTime.now()).build())
        .flatMap(this::save);
  }

  @Override public Mono<Boolean> exists(UUID imageId) {
    return Mono.fromCallable(() -> repository.existsById(imageId));
  }

  @Override public Mono<ImageMetadata> findById(UUID metadataId) {
    return Mono.fromCallable(() -> repository
        .findById(metadataId)
        .orElseThrow(() -> new NotFoundException(String.format(
            "File not found for id %s",
            metadataId))));
  }

  @Override public Flux<ImageMetadata> findAllExistedAt(ZonedDateTime time) {
    return Mono
        .fromCallable(() -> repository.findAllByEntryTimeAfterAndExitTimeBefore(time, time))
        .flatMapMany(Flux::fromIterable);
  }

  @Override public Flux<ImageMetadata> findAllExistedAt(ZonedDateTime from, ZonedDateTime to) {
    return Mono
        .fromCallable(() -> repository.findAllByEntryTimeAfterAndExitTimeBefore(from, to))
        .flatMapMany(Flux::fromIterable);
  }

  @Override public Flux<ImageMetadata> findAll() {
    return Mono.fromCallable(repository::findAll).flatMapMany(Flux::fromIterable);
  }
}
