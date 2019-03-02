package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users_groups")
public class UserGroup extends BaseEntity {

  private UUID userId;
  private UUID groupId;

  public UserGroup(
      UUID id,
      UUID userId,
      UUID groupId,
      ZonedDateTime createdDate,
      ZonedDateTime lastModifiedDate) {
    super(id, createdDate, lastModifiedDate);
    this.userId = userId;
    this.groupId = groupId;
  }

  UserGroup() {
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getGroupId() {
    return groupId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserGroup)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    UserGroup userGroup = (UserGroup) o;
    return Objects.equals(getUserId(), userGroup.getUserId()) &&
        Objects.equals(getGroupId(), userGroup.getGroupId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getUserId(), getGroupId());
  }
}
