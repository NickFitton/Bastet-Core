package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.ImageMetadataV1;
import com.nfitton.imagestorage.api.TallyPointV1;
import com.nfitton.imagestorage.entity.ImageMetadata;
import com.nfitton.imagestorage.model.TallyPoint;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ImageMetadataMapper {

  /**
   * Takes the received metadata and returns a constructed {@link ImageMetadata}.
   *
   * @param v1 the metadata received
   * @param cameraId the id of the saver of the data
   * @return a valid {@link ImageMetadata} created now
   */
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

  /**
   * Takes the outgoing metadata, and converts it to a {@link ImageMetadataV1} to return.
   *
   * @param metadata the user to convert
   * @return the {@link ImageMetadataV1}
   */
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
}
