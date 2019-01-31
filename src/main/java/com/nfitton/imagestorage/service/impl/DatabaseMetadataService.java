package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.entity.ImageEntity.Builder;
import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.model.ImageData;
import com.nfitton.imagestorage.model.TallyPoint;
import com.nfitton.imagestorage.model.TimeFrame;
import com.nfitton.imagestorage.repository.FileEntityRepository;
import com.nfitton.imagestorage.repository.FileMetadataRepository;
import com.nfitton.imagestorage.service.FileMetadataService;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DatabaseMetadataService implements FileMetadataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMetadataService.class);
  private final FileMetadataRepository metadataRepository;
  private final FileEntityRepository entityRepository;

  @Autowired
  public DatabaseMetadataService(
      FileMetadataRepository metadataRepository,
      FileEntityRepository entityRepository) {
    this.metadataRepository = metadataRepository;
    this.entityRepository = entityRepository;
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

  private static Throwable notFound(UUID metadataId) {
    return new NotFoundException(String.format("Metadata by id %s not found", metadataId));
  }

  @Override
  public Mono<ImageData> save(ImageData data) {
    LOGGER.debug("Saving image data");
    Mono<ImageMetadata> savedMetadata = Mono
        .fromCallable(() -> metadataRepository.save(data.asMetadata()));
    if (data.getId() != null) {
      return imageUploaded(data.getId(), data.getEntities());
    } else {
      return savedMetadata
          .map(metadata -> ImageData.Builder.clone(metadata, new LinkedList<>()).build());
    }
  }

  @Override
  public Mono<ImageData> imageUploaded(UUID imageId, List<ImageEntity> entities) {
    LOGGER.debug("Updating image {} with entities", imageId);
    return Mono.fromCallable(() -> metadataRepository.existsById(imageId))
        .flatMap(imageExists -> {
          if (imageExists) {
            List<ImageEntity> connectedEntities = entities.stream()
                .map(entity -> Builder.clone(entity).withMetadataId(imageId).build())
                .collect(Collectors.toList());

            Mono<ImageMetadata> updatedMetadata = Mono
                .fromCallable(() -> metadataRepository.findById(imageId))
                .map(Optional::get)
                .map(metadata -> ImageMetadata.Builder.clone(metadata)
                    .withUpdatedAt(ZonedDateTime.now()).withFileExists(true).build())
                .flatMap(metadata -> Mono.fromCallable(() -> metadataRepository.save(metadata)));

            Mono<List<ImageEntity>> savedEntities = Mono
                .fromCallable(() -> entityRepository.saveAll(connectedEntities));

            return Mono.zip(updatedMetadata, savedEntities)
                .map(tuple -> ImageData.Builder.clone(tuple.getT1(), tuple.getT2()).build());
          }
          return Mono.error(notFound(imageId));
        });
  }

  @Override
  public Mono<Boolean> exists(UUID imageId) {
    return Mono.fromCallable(() -> metadataRepository.existsById(imageId));
  }

  @Override
  public Mono<ImageData> findById(UUID metadataId) {
    LOGGER.debug("Finding data by id {}", metadataId);
    Mono<Optional<ImageMetadata>> optionalMono = Mono
        .fromCallable(() -> metadataRepository.findById(metadataId));
    Mono<List<ImageEntity>> entities = Mono
        .fromCallable(() -> entityRepository.findAllByMetadataId(metadataId));

    return Mono.zip(optionalMono, entities)
        .flatMap(tuple -> {
          if (tuple.getT1().isPresent()) {
            LOGGER.debug("Metadata has {} connected entities", tuple.getT2().size());
            return Mono.just(ImageData.Builder.clone(tuple.getT1().get(), tuple.getT2()).build());
          }
          return Mono.error(notFound(metadataId));
        });
  }

  @Override
  public Flux<ImageData> findAllExistedAt(ZonedDateTime time) {
    return Mono
        .fromCallable(() -> metadataRepository.findAllByEntryTimeAfterAndExitTimeBefore(time, time))
        .flatMapMany(this::findEntitiesByMetadata);
  }

  @Override
  public Flux<ImageData> findAllExistedAt(ZonedDateTime from, ZonedDateTime to) {
    return Mono
        .fromCallable(() -> metadataRepository.findAllByEntryTimeAfterAndExitTimeBefore(from, to))
        .flatMapMany(this::findEntitiesByMetadata);
  }

  @Override
  public Flux<ImageData> findAllByCameraId(UUID cameraId, ZonedDateTime from, ZonedDateTime to) {
    return Mono.fromCallable(() -> metadataRepository
        .findAllByCameraIdAndEntryTimeAfterAndExitTimeBefore(cameraId, from, to))
        .flatMapMany(this::findEntitiesByMetadata);
  }

  private Flux<ImageData> findEntitiesByMetadata(List<ImageMetadata> metadata) {
    return Flux.fromIterable(metadata)
        .flatMap(imageMetadata -> Mono
            .fromCallable(() -> entityRepository.findAllByMetadataId(imageMetadata.getId()))
            .map(entities -> ImageData.Builder.clone(imageMetadata, entities).build()));
  }

  @Override
  public Flux<TallyPoint> countAllExistedAt(
      ZonedDateTime start,
      ZonedDateTime end,
      TimeFrame measurement) {
    List<TallyPoint> points = new LinkedList<>();
    while (advance(start, measurement).isBefore(end)) {
      ZonedDateTime tempEnd = advance(start, measurement);
      long count = metadataRepository.countAllByEntryTimeAfterAndExitTimeBefore(start, tempEnd);
      points.add(new TallyPoint(start, count));
      start = advance(start, measurement);
    }
    return Flux.fromIterable(points);
  }

  @Override
  public Flux<ImageData> findAll() {
    return Mono.fromCallable(metadataRepository::findAll)
        .flatMapMany(this::findEntitiesByMetadata);
  }
}
