package com.nfitton.imagestorage.service.impl;

import static com.nfitton.imagestorage.util.EncryptionUtil.loadRsaKeys;

import com.nfitton.imagestorage.configuration.PathConfiguration;
import com.nfitton.imagestorage.exception.EncryptionException;
import com.nfitton.imagestorage.exception.StartupException;
import com.nfitton.imagestorage.service.FileUploadService;
import com.nfitton.imagestorage.util.EncryptionUtil;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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

  private String path;
  private KeyPair rsaKeys;

  public LocalFileStorage(PathConfiguration configuration) {
    path = configuration.getLocation();
    rsaKeys = loadRsaKeys(path).orElseGet(() -> {
      try {
        return EncryptionUtil.generateRsaKeys(path);
      } catch (NoSuchAlgorithmException e) {
        throw new StartupException("Failed to create new RSA keys");
      }
    });
  }

  private static void close(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      LOGGER.error("Failed to close resource", e);
    }
  }

  @Override
  public Mono<String> uploadFile(FilePart file, UUID imageId) {
    String imagePath = getTempFilePath(imageId) + ".jpg";
    return file.transferTo(new File(imagePath))
        .then(Mono.fromCallable(() -> {
          try {
            EncryptionUtil.encryptWithRsaAes(
                rsaKeys.getPrivate(), imagePath, getEncryptFilePath(imageId));
          } catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException
              | InvalidKeyException | BadPaddingException | IllegalBlockSizeException
              | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Failed to encrypt file", e);
          }
          return imagePath;
        }));
  }

  @Override
  public Flux<byte[]> downloadFile(UUID imageId) {
    EncryptionUtil.decryptWithRsaAes(
        rsaKeys.getPublic(),
        getEncryptFilePath(imageId) + ".enc",
        getTempFilePath(imageId));

    Path filePath = Paths.get(getTempFilePath(imageId) + ".ver");
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

  private String getTempFilePath(UUID imageId) {
    return path + imageId.toString();
  }

  private String getEncryptFilePath(UUID imageId) {
    return path + imageId;
  }
}
