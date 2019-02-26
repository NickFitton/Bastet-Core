package com.nfitton.imagestorage.entity;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "users_groups")
public class UserGroup {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private UUID userId;
  private UUID groupId;

  public UserGroup(UUID id, UUID userId, UUID groupId) {
    this.id = id;
    this.userId = userId;
    this.groupId = groupId;
  }

  UserGroup() {
  }

  public UUID getId() {
    return id;
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
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGroup userGroup = (UserGroup) o;
    return Objects.equals(id, userGroup.id)
        && Objects.equals(userId, userGroup.userId)
        && Objects.equals(groupId, userGroup.groupId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, groupId);
  }
}
