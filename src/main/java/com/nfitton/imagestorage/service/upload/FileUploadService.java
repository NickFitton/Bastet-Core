package com.nfitton.imagestorage.service.upload;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FileUploadService {

  Mono<Void> uploadFile(FilePart file, UUID imageId);

  Flux<byte[]> downloadFile(UUID imageId);
}
