package com.nfitton.imagestorage.service.impl;

import static com.nfitton.imagestorage.entity.AccountType.ADMIN;

import com.nfitton.imagestorage.entity.AccountType;
import com.nfitton.imagestorage.entity.User;
import com.nfitton.imagestorage.exception.ConflictException;
import com.nfitton.imagestorage.exception.InternalServerException;
import com.nfitton.imagestorage.exception.NotFoundException;
import com.nfitton.imagestorage.repository.AccountRepository;
import com.nfitton.imagestorage.service.AuthenticationService;
import com.nfitton.imagestorage.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DatabaseUserService implements UserService {

  private final AccountRepository repository;
  private final AuthenticationService authenticationService;

  @Autowired
  public DatabaseUserService(
      AccountRepository accountRepository,
      AuthenticationService authenticationService) {
    repository = accountRepository;
    this.authenticationService = authenticationService;
  }

  @Override
  public Mono<Optional<User>> findById(UUID id) {
    return Mono.fromCallable(() -> repository.findById(id));
  }

  @Override
  public Mono<User> save(User account) {
    return Mono.fromCallable(() -> repository.save(account))
        .onErrorResume(e -> {
          if (e instanceof DataIntegrityViolationException) {
            ConstraintViolationException exception = (ConstraintViolationException) e.getCause();
            String sqlState = exception.getSQLException().getSQLState();

            if (sqlState.equals("23505")) {
              return Mono.error(new ConflictException(
                  String.format("Email already in use: %s", account.getEmail())));
            }
            return Mono.error(e);
          }
          return Mono.error(e);
        });
  }

  @Override
  public Mono<UUID> authenticate(String email, String password, AccountType type) {
    switch (type) {
      case ADMIN:
      case BASIC:
        return authenticationService.authenticateEmail(email, password, type, repository);
      default:
        throw new InternalServerException(
            String.format("User service can't authenticate given type: %s", type));
    }
  }

  @Override
  public Flux<UUID> getAllIds() {
    return Mono.fromCallable(repository::findAllAccount).flatMapIterable(ids -> ids);
  }

  @Override
  public Mono<Boolean> existsById(UUID uuid) {
    return Mono.fromCallable(() -> repository.existsById(uuid));
  }

  @Override
  public Mono<Boolean> deleteById(UUID id) {
    return Mono.fromCallable(() -> {
      repository.deleteById(id);
      return true;
    });
  }

  @Override
  public Mono<Boolean> idIsAdmin(UUID userId) {
    return findById(userId)
        .map(optionalUser -> optionalUser.map(user -> user.getType() == ADMIN).orElse(false));
  }

  @Override
  public Mono<User> findByEmail(String email) {
    return Mono.fromCallable(() -> repository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found by given email address")));
  }
}
