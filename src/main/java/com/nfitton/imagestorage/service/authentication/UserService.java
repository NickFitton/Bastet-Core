package com.nfitton.imagestorage.service.authentication;

import com.nfitton.imagestorage.entity.Account;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {

  Mono<Account> createAccount(Account newAccount);

  Mono<Account> getAccountById(UUID id);

  Mono<Boolean> existsById(UUID id);
}
