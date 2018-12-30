package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.ImageEntityV1;
import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.TallyPointV1;
import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.model.TallyPoint;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageMetadataMapper {

  public static ImageMetadata newMetadata(ImageMetadataV1 v1) {
    ZonedDateTime now = ZonedDateTime.now();
    return ImageMetadata.Builder
        .newBuilder()
        .withEntryTime(v1.getEntryTime())
        .withExitTime(v1.getExitTime())
        .withImageTime(v1.getImageTime())
        .withCreatedAt(now)
        .withUpdatedAt(now)
        .build();
  }

  public static ImageMetadata newMetadata(ImageMetadataV1 v1, UUID cameraId) {
    ZonedDateTime now = ZonedDateTime.now();
    return ImageMetadata.Builder
        .newBuilder()
        .withCameraId(cameraId)
        .withEntryTime(v1.getEntryTime())
        .withExitTime(v1.getExitTime())
        .withImageTime(v1.getImageTime())
        .withCreatedAt(now)
        .withUpdatedAt(now)
        .build();
  }

  public static ImageMetadataV1 toV1(ImageMetadata metadata) {
    return new ImageMetadataV1(
        metadata.getId(),
        metadata.getEntryTime(),
        metadata.getExitTime(),
        metadata.getImageTime(),
        metadata.getCreatedAt(),
        metadata.getUpdatedAt(),
        metadata.fileExists(),
        toV1(metadata.getImageEntities()));
  }

  private static Set<ImageEntityV1> toV1(Set<ImageEntity> entities) {
    return entities.stream().map(ImageMetadataMapper::toV1).collect(Collectors.toSet());
  }

  private static ImageEntityV1 toV1(ImageEntity entity) {
    return new ImageEntityV1(entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight(),
                             entity.getType());
  }

  public static TallyPointV1 toV1(TallyPoint point) {
    return new TallyPointV1(point.getTime(), point.getCount());
  }
}
