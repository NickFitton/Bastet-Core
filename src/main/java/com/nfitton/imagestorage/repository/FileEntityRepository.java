package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.ImageEntity;
import com.nfitton.imagestorage.entity.ImageMetadata;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileEntityRepository extends JpaRepository<ImageEntity, UUID> {
  List<ImageEntity> findAllByMetadataId(UUID id);
}
