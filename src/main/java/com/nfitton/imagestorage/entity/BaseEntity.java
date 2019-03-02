package com.nfitton.imagestorage.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@MappedSuperclass
public class BaseEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;
  @CreatedDate
  private ZonedDateTime createdDate;
  @LastModifiedDate
  private ZonedDateTime lastModifiedDate;

  public BaseEntity(UUID id, ZonedDateTime createdDate, ZonedDateTime lastModifiedDate) {
    this.id = id;
    this.createdDate = createdDate;
    this.lastModifiedDate = lastModifiedDate;
  }

  public BaseEntity() {}

  public UUID getId() {
    return id;
  }

  public ZonedDateTime getCreatedDate() {
    return createdDate;
  }

  public ZonedDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseEntity)) {
      return false;
    }
    BaseEntity that = (BaseEntity) o;
    return Objects.equals(getId(), that.getId()) &&
        Objects.equals(getCreatedDate(), that.getCreatedDate()) &&
        Objects.equals(getLastModifiedDate(), that.getLastModifiedDate());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getCreatedDate(), getLastModifiedDate());
  }
}
