package com.nfitton.imagestorage.service;

import com.nfitton.imagestorage.entity.AccountType;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService<T, ID> {

  Mono<T> save(T account);

  Mono<ID> authenticate(String id, String password, AccountType type);

  Mono<Optional<T>> findById(ID id);

  Flux<ID> getAllIds();

  Mono<Boolean> deleteById(ID id);


}
