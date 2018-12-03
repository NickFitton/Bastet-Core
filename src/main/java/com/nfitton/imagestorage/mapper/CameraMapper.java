package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.entity.Camera;

public class CameraMapper {

  public static Camera toEntity(CameraV1 v1) {
    return Camera.Builder
        .newBuilder()
        .withId(v1.getId())
        .withName(v1.getName())
        .withCreatedAt(v1.getCreatedAt())
        .withUpdatedAt(v1.getUpdatedAt())
        .withLastUpload(v1.getLastUpload())
        .build();
  }

  public static CameraV1 toApiBean(Camera camera) {
    return new CameraV1(
        camera.getId(),
        camera.getName(),
        camera.getCreatedAt(),
        camera.getUpdatedAt(),
        camera.getLastUpload());
  }
}
