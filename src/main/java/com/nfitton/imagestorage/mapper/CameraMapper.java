package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.CameraV1;
import com.nfitton.imagestorage.entity.Camera;
import com.nfitton.imagestorage.util.ValidationUtil;
import javax.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CameraMapper {

  /**
   * Takes the received camera data, encodes the password and returns a constructed {@link Camera}.
   *
   * @param v1 the camera data received
   * @param encoder the encoder for the password
   * @param validator the validator for the data
   * @return a valid {@link Camera}
   */
  public static Camera toEntity(CameraV1 v1, PasswordEncoder encoder, Validator validator) {
    ValidationUtil.validate(v1, validator);

    return Camera.Builder
        .newBuilder()
        .withId(v1.getId())
        .withPassword(encoder.encode(v1.getPassword()))
        .withCreatedAt(v1.getCreatedAt())
        .withUpdatedAt(v1.getUpdatedAt())
        .withLastActive(v1.getLastUpload())
        .build();
  }

  /**
   * Takes the outgoing camera data, and converts it to a {@link CameraV1} to return.
   *
   * @param camera the camera to convert
   * @return the {@link CameraV1} without exposing the password
   */
  public static CameraV1 toApiBean(Camera camera) {
    return new CameraV1(
        camera.getId(),
        null,
        camera.getCreatedAt(),
        camera.getUpdatedAt(),
        camera.getLastActive());
  }
}
