package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@JsonInclude(Include.NON_NULL)
public class UserV1 {

  private UUID id;
  @NotBlank(message = "A first name must be given")
  @Pattern(regexp = "[A-Za-z]*")
  @Size(min = 2, max = 32)
  private String firstName;
  @NotBlank(message = "A last name must be given")
  @Pattern(regexp = "[A-Za-z]*")
  @Size(min = 2, max = 32)
  private String lastName;
  @NotBlank(message = "An email must be given")
  @Email
  @Size(min = 4, max = 64)
  private String email;
  @NotBlank(message = "A password must be given")
  @Size(min = 6, max = 64)
  private String password;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime lastActive;

  @JsonCreator
  public UserV1(
      @JsonProperty("id") UUID id,
      @JsonProperty("firstName") String firstName,
      @JsonProperty("lastName") String lastName,
      @JsonProperty("email") String email,
      @JsonProperty("password") String password,
      @JsonProperty("createdAt") ZonedDateTime createdAt,
      @JsonProperty("updatedAt") ZonedDateTime updatedAt,
      @JsonProperty("lastActive") ZonedDateTime lastActive) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.lastActive = lastActive;
  }

  public UUID getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
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

  public static final class Builder {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastActive;

    private Builder() {
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder withId(UUID val) {
      this.id = val;
      return this;
    }

    public Builder withFirstName(String val) {
      this.firstName = val;
      return this;
    }

    public Builder withLastName(String val) {
      this.lastName = val;
      return this;
    }

    public Builder withEmail(String val) {
      this.email = val;
      return this;
    }

    public Builder withPassword(String val) {
      this.password = val;
      return this;
    }

    public Builder withCreatedAt(ZonedDateTime val) {
      this.createdAt = val;
      return this;
    }

    public Builder withUpdatedAt(ZonedDateTime val) {
      this.updatedAt = val;
      return this;
    }

    public Builder withLastActive(ZonedDateTime val) {
      this.lastActive = val;
      return this;
    }

    public UserV1 build() {
      return new UserV1(id, firstName, lastName, email, password, createdAt, updatedAt, lastActive);
    }
  }
}
