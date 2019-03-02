package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "groups")
public class Group extends BaseEntity {

  private UUID ownerId;
  private String name;

  public Group(
      UUID id,
      UUID ownerId,
      String name,
      ZonedDateTime createdDate,
      ZonedDateTime lastModifiedDate) {
    super(id, createdDate, lastModifiedDate);
    this.ownerId = ownerId;
    this.name = name;
  }

  public Group() {

  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Group)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Group group = (Group) o;
    return Objects.equals(getOwnerId(), group.getOwnerId()) &&
        Objects.equals(getName(), group.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getOwnerId(), getName());
  }
}
