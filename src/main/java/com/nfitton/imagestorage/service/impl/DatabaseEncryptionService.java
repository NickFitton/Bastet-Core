package com.nfitton.imagestorage.service.impl;

import com.nfitton.imagestorage.repository.CameraKeyRepository;
import com.nfitton.imagestorage.service.EncryptionService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DatabaseEncryptionService implements EncryptionService {

  private CameraKeyRepository keyRepository;

  @Autowired public DatabaseEncryptionService(CameraKeyRepository keyRepository) {
    this.keyRepository = keyRepository;
  }

  @Override public <T> Mono<T> decryptObject(
      UUID cameraId, String encryptedObject, Class<T> objectClass) {
    return null;
  }

  @Override public <T> Mono<String> encryptObject(UUID cameraId, T object, Class<T> objectClass) {
    return null;
  }
}
