package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.ImageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileEntityRepository extends JpaRepository<ImageEntity, UUID> {

  List<ImageEntity> findAllByMetadataId(UUID id);
}
