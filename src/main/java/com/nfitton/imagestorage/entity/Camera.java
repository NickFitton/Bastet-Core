package com.nfitton.imagestorage.entity;

import static com.nfitton.imagestorage.entity.AccountType.CAMERA;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Entity;

@Entity
public class Camera extends Account {

  public Camera(
      UUID id,
      String password,
      ZonedDateTime createdAt,
      ZonedDateTime updatedAt,
      ZonedDateTime lastActive) {
    super(id, password, CAMERA, createdAt, updatedAt, lastActive);
  }

  private Camera() {
    super();
  }

  public static final class Builder {

    private UUID id;
    private String password;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastUpload;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Hard copies the given camera object into a {@link Camera.Builder}.
     * @param camera the camera to copy
     * @return a {@link Camera.Builder} that can have its values edited
     */
    public static Builder clone(Camera camera) {
      return new Builder()
          .withId(camera.getId())
          .withPassword(camera.getPassword())
          .withCreatedAt(camera.getCreatedAt())
          .withUpdatedAt(camera.getUpdatedAt())
          .withLastActive(camera.getLastActive());
    }

    public Builder withId(UUID val) {
      id = val;
      return this;
    }

    public Builder withPassword(String val) {
      password = val;
      return this;
    }

    public Builder withCreatedAt(ZonedDateTime val) {
      createdAt = val;
      return this;
    }

    public Builder withUpdatedAt(ZonedDateTime val) {
      updatedAt = val;
      return this;
    }

    public Builder withLastActive(ZonedDateTime val) {
      lastUpload = val;
      return this;
    }

    public Camera build() {
      return new Camera(id, password, createdAt, updatedAt, lastUpload);
    }

  }
}
