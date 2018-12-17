package com.nfitton.imagestorage.handler;

import static com.nfitton.imagestorage.util.RouterUtil.getUUIDParameter;

import com.nfitton.imagestorage.api.AccountV1;
import com.nfitton.imagestorage.api.OutgoingDataV1;
import com.nfitton.imagestorage.mapper.AccountMapper;
import com.nfitton.imagestorage.service.UserService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class UserHandlerV1 {

  private final PasswordEncoder encoder;
  private final UserService userService;

  @Autowired
  public UserHandlerV1(PasswordEncoder encoder, UserService userService) {
    this.encoder = encoder;
    this.userService = userService;
  }

  public Mono<ServerResponse> createUser(ServerRequest request) {
    Mono<OutgoingDataV1> createdAccount = request.bodyToMono(AccountV1.class)
        .map((AccountV1 v1) -> AccountMapper.newAccount(v1, encoder))
        .flatMap(userService::save)
        .map(AccountMapper::toV1)
        .map(account -> new OutgoingDataV1(account, null));

    return ServerResponse.status(HttpStatus.CREATED).body(createdAccount, OutgoingDataV1.class);
  }

  public Mono<ServerResponse> getUsers(ServerRequest request) {

    Mono<OutgoingDataV1> userIds = userService.getAllIds().collectList()
        .map(ids -> new OutgoingDataV1(ids, null));

    return ServerResponse.ok().body(userIds, OutgoingDataV1.class);
  }

  public Mono<ServerResponse> getUser(ServerRequest request) {
    UUID userId = getUUIDParameter(request, "userId");
    Mono<OutgoingDataV1> foundAccount = userService.findById(userId)
        .map(optionalUser -> optionalUser.map(AccountMapper::toV1)
            .orElse(new AccountV1(null, null, null, null, null, null, null)))
        .map(account -> new OutgoingDataV1(account, null));

    return ServerResponse.ok().body(foundAccount, OutgoingDataV1.class);
  }

  public Mono<ServerResponse> deleteUser(ServerRequest request) {
    UUID userId = getUUIDParameter(request, "userId");
    return userService.deleteById(userId)
        .flatMap(deleted -> {
          if (deleted) {
            return ServerResponse.noContent().build();
          } else {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        });
  }
}
