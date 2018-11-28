package com.nfitton.imagestorage.mapper;

import com.nfitton.imagestorage.api.AccountV1;
import com.nfitton.imagestorage.entity.Account;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.ZonedDateTime;

public class AccountMapper {

  public static Account newAccount(AccountV1 v1) {
    ZonedDateTime now = ZonedDateTime.now();
    String salt = BCrypt.gensalt();

    return Account.Builder
        .newBuilder()
        .withName(v1.getName())
        .withEmail(v1.getEmail())
        .withPassword(BCrypt.hashpw(v1.getPassword(), salt))
        .withSalt(salt)
        .withCreatedAt(now)
        .withUpdatedAt(now)
        .withLastActive(now)
        .build();
  }

  public static Account newPassword(Account oldAccount, String newPassword) {
    String salt = BCrypt.gensalt();
    return Account.Builder
        .clone(oldAccount)
        .withSalt(salt)
        .withPassword(BCrypt.hashpw(newPassword, salt))
        .build();
  }
}
