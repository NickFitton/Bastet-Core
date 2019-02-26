package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.util.ValidationUtil;
import javax.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CameraMapper {

  /**
   * Creates a new camera from a given CameraV1 validates it with the given validator and encodes
   * the password with the given encoder.
   *
   * @param v1 the camera to convert
   * @param encoder encoder for the password
   * @param validator validator for the UserV1 bean
   * @return a {@link Camera} entity to save
   */
  public static Camera toEntity(CameraV1 v1, PasswordEncoder encoder, Validator validator) {
    ValidationUtil.validate(v1, validator);

    return Camera.Builder
        .newBuilder()
        .withId(v1.getId())
        .withOwnerId(v1.getOwnedBy())
        .withName(v1.getName())
        .withPassword(encoder.encode(v1.getPassword()))
        .withCreatedAt(v1.getCreatedAt())
        .withUpdatedAt(v1.getUpdatedAt())
        .withLastActive(v1.getLastUpload())
        .build();
  }

  public static Camera toEntity(CameraV1 v1) {
    return Camera.Builder
        .newBuilder()
        .withId(v1.getId())
        .withOwnerId(v1.getOwnedBy())
        .withName(v1.getName())
        .withCreatedAt(v1.getCreatedAt())
        .withUpdatedAt(v1.getUpdatedAt())
        .withLastActive(v1.getLastUpload())
        .build();
  }

  public static CameraV1 toApiBean(Camera camera) {
    return new CameraV1(
        camera.getId(),
        camera.getOwnerId(),
        camera.getName() == null ? camera.getId().toString() : camera.getName(),
        null,
        camera.getCreatedAt(),
        camera.getUpdatedAt(),
        camera.getLastActive());
  }
}
