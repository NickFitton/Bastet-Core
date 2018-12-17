package com.nfitton.imagestorage.mapper;

import static com.nfitton.imagestorage.entity.AccountType.BASIC;

import com.nfitton.imagestorage.api.AccountV1;
import com.nfitton.imagestorage.entity.User;
import java.time.ZonedDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AccountMapper {

  public static User newAccount(AccountV1 v1, PasswordEncoder encoder) {
    ZonedDateTime now = ZonedDateTime.now();

    return User.Builder
        .newBuilder()
        .withName(v1.getName())
        .withEmail(v1.getEmail())
        .withPassword(encoder.encode(v1.getPassword()))
        .withType(BASIC)
        .withCreatedAt(now)
        .withUpdatedAt(now)
        .withLastActive(now)
        .build();
  }

  public static AccountV1 toV1(User user) {
    return new AccountV1(
        user.getId(),
        user.getName(),
        user.getEmail(),
        null,
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getLastActive());
  }
}
