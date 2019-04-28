package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class GroupV1 {

  private UUID id;
  private UUID ownedBy;
  @NotNull(message = "The group must have a name")
  private String name;
  private List<UUID> users;
  private List<UUID> cameras;

  @JsonCreator
  public GroupV1(
      @JsonProperty("id") UUID id,
      @JsonProperty("ownedBy") UUID ownedBy,
      @JsonProperty("name") String name,
      @JsonProperty("users") List<UUID> users,
      @JsonProperty("cameras") List<UUID> cameras) {
    this.id = id;
    this.ownedBy = ownedBy;
    this.name = name;
    this.users = users;
    this.cameras = cameras;
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

  public List<UUID> getUsers() {
    return users;
  }

  public List<UUID> getCameras() {
    return cameras;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupV1 groupV1 = (GroupV1) o;
    return Objects.equals(id, groupV1.id)
        && Objects.equals(ownedBy, groupV1.ownedBy)
        && Objects.equals(name, groupV1.name)
        && Objects.equals(users, groupV1.users)
        && Objects.equals(cameras, groupV1.cameras);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, ownedBy, name, users, cameras);
  }
}
