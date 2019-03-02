package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "groups_cameras")
public class GroupCamera extends BaseEntity {

  private UUID groupId;
  private UUID cameraId;

  public GroupCamera(
      UUID id,
      UUID groupId,
      UUID cameraId,
      ZonedDateTime createdDate,
      ZonedDateTime lastModifiedDate) {
    super(id, createdDate, lastModifiedDate);
    this.groupId = groupId;
    this.cameraId = cameraId;
  }

  GroupCamera() {
  }

  public UUID getGroupId() {
    return groupId;
  }

  public UUID getCameraId() {
    return cameraId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GroupCamera)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    GroupCamera that = (GroupCamera) o;
    return Objects.equals(getGroupId(), that.getGroupId()) &&
        Objects.equals(getCameraId(), that.getCameraId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getGroupId(), getCameraId());
  }
}
