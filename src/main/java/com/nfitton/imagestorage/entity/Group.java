package com.nfitton.imagestorage.entity;

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "groups")
public class Group {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  private UUID ownerId;
  private String name;

  public Group(UUID id, UUID ownerId, String name) {
    this.id = id;
    this.ownerId = ownerId;
    this.name = name;
  }

  public Group() {

  }

  public UUID getId() {
    return id;
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
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Group group = (Group) o;
    return Objects.equals(id, group.id)
        && Objects.equals(ownerId, group.ownerId)
        && Objects.equals(name, group.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ownerId, name);
  }

}
