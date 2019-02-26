package com.nfitton.imagestorage.entity;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "groups_cameras")
public class GroupCamera {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private UUID groupId;
  private UUID cameraId;

  public GroupCamera(UUID id, UUID groupId, UUID cameraId) {
    this.id = id;
    this.groupId = groupId;
    this.cameraId = cameraId;
  }

  GroupCamera() {

  }

  public UUID getId() {
    return id;
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
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupCamera that = (GroupCamera) o;
    return Objects.equals(id, that.id)
        && Objects.equals(groupId, that.groupId)
        && Objects.equals(cameraId, that.cameraId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, groupId, cameraId);
  }
}
