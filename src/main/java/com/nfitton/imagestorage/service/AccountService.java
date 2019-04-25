package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.AccountType;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService<T, I> {

  Mono<T> save(T account);

  Mono<I> authenticate(String id, String password, AccountType type);

  Mono<Optional<T>> findById(I id);

  Flux<I> getAllIds();

  Mono<Boolean> existsById(I id);

  Mono<Boolean> deleteById(I id);
}
