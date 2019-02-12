package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.UserGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {

  List<UserGroup> findAllByGroupId(UUID groupId);

//  Optional<UserGroup> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
