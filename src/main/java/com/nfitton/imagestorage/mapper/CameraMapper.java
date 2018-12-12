package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.entity.Camera;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CameraMapper {

  public static Camera toEntity(CameraV1 v1, PasswordEncoder encoder) {
    return Camera.Builder
        .newBuilder()
        .withId(v1.getId())
        .withPassword(encoder.encode(v1.getPassword()))
        .withCreatedAt(v1.getCreatedAt())
        .withUpdatedAt(v1.getUpdatedAt())
        .withLastUpload(v1.getLastUpload())
        .build();
  }

  public static CameraV1 toApiBean(Camera camera) {
    return new CameraV1(
        camera.getId(),
        null,
        camera.getCreatedAt(),
        camera.getUpdatedAt(),
        camera.getLastUpload());
  }
}
