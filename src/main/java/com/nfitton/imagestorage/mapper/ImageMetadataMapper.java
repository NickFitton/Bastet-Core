package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.TallyPointV1;
import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.model.TallyPoint;

import java.time.ZonedDateTime;
import java.util.UUID;

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
        metadata.fileExists());
  }

  public static TallyPointV1 toV1(TallyPoint point) {
    return new TallyPointV1(point.getTime(), point.getCount());
  }
}
