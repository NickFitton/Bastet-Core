package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.GroupCamera;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupCameraRepository extends JpaRepository<GroupCamera, UUID> {
  List<GroupCamera> findAllByGroupId(UUID groupId);
}
