package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.model.ImageData;
import com.nfitton.imagestorage.model.TallyPoint;
import com.nfitton.imagestorage.model.TimeFrame;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileMetadataService {

  Mono<ImageData> save(ImageData data);

  Mono<ImageData> imageUploaded(UUID imageId, List<ImageEntity> entities);

  Mono<Boolean> exists(UUID imageId);

  Mono<ImageData> findById(UUID metadataId);

  Flux<ImageData> findAllExistedAt(ZonedDateTime time);

  Flux<ImageData> findAllExistedAt(ZonedDateTime from, ZonedDateTime to);

  Flux<ImageData> findAllByCameraId(UUID cameraId, ZonedDateTime from, ZonedDateTime to);

  Flux<TallyPoint> countAllExistedAt(ZonedDateTime start, ZonedDateTime end, TimeFrame measurement);

  Flux<ImageData> findAll();
}
