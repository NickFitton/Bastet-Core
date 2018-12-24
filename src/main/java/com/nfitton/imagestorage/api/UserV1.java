package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class UserV1 {

  private UUID id;
  @NotNull(message = "A name must be given")
  private String name;
  @NotNull(message = "An email must be given")
  private String email;
  @NotNull(message = "A password must be given")
  private String password;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastActive;

  /**
   * Standard JSON creator, see {@link JsonCreator}.
   */
  @JsonCreator
  public UserV1(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("email") String email,
      @JsonProperty("password") String password,
      @JsonProperty("createdAt") ZonedDateTime createdAt,
      @JsonProperty("updatedAt") ZonedDateTime updatedAt,
      @JsonProperty("lastActive") ZonedDateTime lastActive) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastActive = lastActive;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
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

  public ZonedDateTime getLastActive() {
    return lastActive;
  }
}
