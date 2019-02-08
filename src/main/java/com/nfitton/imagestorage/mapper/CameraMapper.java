package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.util.ValidationUtil;
import javax.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CameraMapper {

  public static Camera toEntity(CameraV1 v1, PasswordEncoder encoder, Validator validator) {
    ValidationUtil.validate(v1, validator);

    return Camera.Builder
        .newBuilder()
        .withId(v1.getId())
        .withOwnedBy(v1.getOwnedBy())
        .withPassword(encoder.encode(v1.getPassword()))
        .withCreatedAt(v1.getCreatedAt())
        .withUpdatedAt(v1.getUpdatedAt())
        .withLastActive(v1.getLastUpload())
        .build();
  }

  public static CameraV1 toApiBean(Camera camera) {
    return new CameraV1(
        camera.getId(),
        camera.getOwnedBy(),
        null,
        camera.getCreatedAt(),
        camera.getUpdatedAt(),
        camera.getLastActive());
  }
}
