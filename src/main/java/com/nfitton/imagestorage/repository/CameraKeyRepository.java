package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.DHKey;
import com.nfitton.imagestorage.entity.KeyType;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraKeyRepository extends JpaRepository<DHKey, UUID> {

  boolean existsByCameraIdAndType(UUID cameraId, KeyType keyType);
}
