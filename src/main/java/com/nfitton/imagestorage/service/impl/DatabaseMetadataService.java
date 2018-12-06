package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.model.TallyPoint;
import com.nfitton.imagestorage.model.TimeFrame;
import com.nfitton.imagestorage.repository.FileMetadataRepository;
import com.nfitton.imagestorage.service.FileMetadataService;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DatabaseMetadataService implements FileMetadataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMetadataService.class);

  private final FileMetadataRepository repository;

  @Autowired public DatabaseMetadataService(FileMetadataRepository repository) {
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
        .orElseThrow(() -> new NotFoundException(String.format("File not found for id %s",
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

  @Override public Flux<TallyPoint> countAllExistedAt(
      ZonedDateTime start, ZonedDateTime end, TimeFrame measurement) {
    List<TallyPoint> points = new LinkedList<>();
    while (advance(start, measurement).isBefore(end)) {
      ZonedDateTime tempEnd = advance(start, measurement);
      long count = repository.countAllByEntryTimeAfterAndExitTimeBefore(start, tempEnd);
      points.add(new TallyPoint(start, count));
      start = advance(start, measurement);
    }

    return Flux.fromIterable(points);
  }

  @Override public Flux<ImageMetadata> findAll() {
    return Mono.fromCallable(repository::findAll).flatMapMany(Flux::fromIterable);
  }

  private static ZonedDateTime advance(ZonedDateTime time, TimeFrame measurement) {
    switch (measurement) {
      case MINUTE:
        return time.plusMinutes(1);
      case HOUR:
        return time.plusHours(1);
      case DAY:
        return time.plusDays(1);
      case WEEK:
        return time.plusDays(7);
      case MONTH:
        return time.plusMonths(1);
      case YEAR:
        return time.plusYears(1);
      default:
        return time.plusHours(1);
    }
  }
}
