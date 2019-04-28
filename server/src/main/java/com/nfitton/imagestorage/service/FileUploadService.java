package com.nfitton.imagestorage.service;

import java.util.UUID;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileUploadService {

  Mono<String> uploadFile(FilePart file, UUID imageId);

  Flux<byte[]> downloadFile(UUID imageId);
}
