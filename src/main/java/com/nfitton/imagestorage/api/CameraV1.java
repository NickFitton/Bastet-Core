package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class CameraV1 {

  private UUID id;
  private UUID ownedBy;
  private String name;
  @NotNull(message = "A password must be given")
  private String password;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastUpload;

  @JsonCreator
  public CameraV1(
      @JsonProperty("id") UUID id,
      @JsonProperty("ownedBy") UUID ownedBy,
      @JsonProperty("name") String name,
      @JsonProperty("password") String password,
      @JsonProperty("createdAt") ZonedDateTime createdAt,
      @JsonProperty("updatedAt") ZonedDateTime updatedAt,
      @JsonProperty("lastUpload") ZonedDateTime lastUpload) {
    this.id = id;
    this.ownedBy = ownedBy;
    this.name = name;
    this.password = password;
    this.createdAt = utcOrNull(createdAt);
    this.updatedAt = utcOrNull(updatedAt);
    this.lastUpload = utcOrNull(lastUpload);
  }

  /**
   * Formats the time to be UTC, workaround to Jackson giving the incorrect zone.
   *
   * @param time the time to parse
   * @return null if time is null, else return the given time with a zone of "UTC"
   */
  private static ZonedDateTime utcOrNull(ZonedDateTime time) {
    if (time == null) {
      return time;
    } else {
      return time.withZoneSameInstant(ZoneId.of("UTC"));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CameraV1 cameraV1 = (CameraV1) o;
    return Objects.equals(id, cameraV1.id)
        && Objects.equals(ownedBy, cameraV1.ownedBy)
        && Objects.equals(password, cameraV1.password)
        && Objects.equals(createdAt, cameraV1.createdAt)
        && Objects.equals(updatedAt, cameraV1.updatedAt)
        && Objects.equals(lastUpload, cameraV1.lastUpload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ownedBy, password, createdAt, updatedAt, lastUpload);
  }

  public UUID getId() {
    return id;
  }

  public UUID getOwnedBy() {
    return ownedBy;
  }

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public ZonedDateTime getLastUpload() {
    return lastUpload;
  }
}
