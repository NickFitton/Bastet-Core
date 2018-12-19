package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.Camera;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraRepository extends JpaRepository<Camera, UUID> {

  @Query("SELECT id FROM Camera")
  List<UUID> findAllCamera();
}
