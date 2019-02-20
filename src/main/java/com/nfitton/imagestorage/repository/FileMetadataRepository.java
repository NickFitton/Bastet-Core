package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.ImageMetadata;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<ImageMetadata, UUID> {

  Optional<ImageMetadata> findById(UUID id);

  List<ImageMetadata> findAllByEntryTimeAfterAndExitTimeBefore(
      ZonedDateTime timeAfterEntry, ZonedDateTime timeBeforeExit);

  long countAllByEntryTimeAfterAndExitTimeBefore(
      ZonedDateTime timeAfterEntry, ZonedDateTime timeBeforeExit);

  List<ImageMetadata> findAllByCameraIdAndEntryTimeAfterAndExitTimeBefore(
      UUID cameraId, ZonedDateTime timeAfterEntry, ZonedDateTime timeBeforeExit);
}
