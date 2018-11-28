package com.nfitton.imagestorage.service.authentication.impl;

import com.nfitton.imagestorage.entity.Account;
import com.nfitton.imagestorage.exception.BadRequestException;
import com.nfitton.imagestorage.repository.AccountRepository;
import com.nfitton.imagestorage.service.authentication.UserService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DatabaseUserService implements UserService {

  private final AccountRepository repository;

  @Autowired public DatabaseUserService(AccountRepository accountRepository) {
    repository = accountRepository;
  }

  @Override public Mono<Account> createAccount(Account newAccount) {
    return Mono.fromCallable(() -> repository.save(newAccount));
  }

  @Override public Mono<Account> getAccountById(UUID id) {
    return Mono
        .fromCallable(() -> repository.findById(id))
        .map(account -> account.orElseThrow(() -> new BadRequestException(
            "Account not found by id given")));
  }

  @Override public Mono<Boolean> existsById(UUID id) {
    return Mono.fromCallable(() -> repository.existsById(id));
  }
}
