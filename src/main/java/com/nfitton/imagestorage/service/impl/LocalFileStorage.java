package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.configuration.PathConfiguration;
import com.nfitton.imagestorage.service.FileUploadService;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Profile("local")
public class LocalFileStorage implements FileUploadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileStorage.class);

  private static String path;

  public LocalFileStorage(PathConfiguration configuration) {
    path = configuration.getLocation();
  }

  private static void close(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      LOGGER.error("Failed to close resource", e);
    }
  }

  /**
   * Delete a file at {@code path}.
   *
   * @param path Path to be deleted
   * @return {@link Mono} completed when file is deleted.
   */
  private static Mono<Void> delete(Path path) {
    return Mono.defer(() -> {
      try {
        LOGGER.trace("Deleting file=[{}]", path);
        Files.deleteIfExists(path);
        return Mono.just(true);
      } catch (IOException e) {
        LOGGER.error("Failed to delete path {} with error {}", path, e);
        return Mono.error(e);
      }
    }).then();
  }

  @Override
  public Mono<String> uploadFile(FilePart file, UUID imageId) {
    String somePath = path + imageId + ".jpg";

    return file.transferTo(new File(somePath)).then(Mono.just(somePath));
  }

  @Override
  public Flux<byte[]> downloadFile(UUID imageId) {
    Path filePath = Paths.get(path + imageId.toString() + ".jpg");
    ByteBuffer buffer = ByteBuffer.allocate(8192);

    return Flux.using(
        () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.READ),
        fileChannel -> readFile(buffer, fileChannel),
        LocalFileStorage::close);
  }

  private Flux<byte[]> readFile(ByteBuffer buffer, AsynchronousFileChannel channel) {
    return Flux
        .range(0, Integer.MAX_VALUE)
        .map(i -> i * 8192)
        .concatMap(pos -> Mono.using(() -> buffer, b -> read(pos, b, channel), ByteBuffer::clear))
        .takeWhile(Optional::isPresent)
        .map(Optional::get);
  }

  private Mono<Optional<byte[]>> read(
      long position, ByteBuffer buffer, AsynchronousFileChannel channel) {
    return Mono.defer(() -> {
      LOGGER.trace("Reading chunk={} at position={}", buffer.capacity(), position);
      CompletableFuture<Optional<byte[]>> future = new CompletableFuture<>();
      channel.read(
          buffer,
          position,
          future,
          new CompletionHandler<Integer, CompletableFuture<Optional<byte[]>>>() {
            @Override
            public void completed(
                Integer read, CompletableFuture<Optional<byte[]>> future) {
              if (read == -1) {
                LOGGER.trace("No chunks at position={}", position);
                future.complete(Optional.empty());
              } else {
                LOGGER.trace(
                    "Finished reading chunk of size={} at position={}",
                    read,
                    position);
                byte[] bytes = new byte[read];
                buffer.rewind();
                buffer.get(bytes);
                future.complete(Optional.of(bytes));
              }
            }

            @Override
            public void failed(
                Throwable throwable, CompletableFuture<Optional<byte[]>> future) {
              LOGGER.trace("Failed to read chunk at position={}", position);
              future.completeExceptionally(throwable);
            }
          });
      return Mono.fromCompletionStage(future);
    });
  }
}
