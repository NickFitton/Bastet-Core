package com.nfitton.imagestorage.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class PasswordV1 {

  @NotNull
  private String currentPassword;
  @NotNull
  private String newPassword;

  @JsonCreator
  public PasswordV1(
      @JsonProperty("currentPassword") String currentPassword,
      @JsonProperty("newPassword") String newPassword) {
    this.currentPassword = currentPassword;
    this.newPassword = newPassword;
  }

  public String getCurrentPassword() {
    return currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PasswordV1 that = (PasswordV1) o;
    return Objects.equals(getCurrentPassword(), that.getCurrentPassword())
        && Objects.equals(getNewPassword(), that.getNewPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCurrentPassword(), getNewPassword());
  }
}
