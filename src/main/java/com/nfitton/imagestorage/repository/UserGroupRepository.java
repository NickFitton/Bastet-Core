package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.UserGroup;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {

  List<UserGroup> findAllByGroupId(UUID groupId);

  List<UserGroup> findAllByUserId(UUID userId);

  @Transactional
  void deleteByUserIdAndGroupId(UUID userId, UUID groupId);

  @Transactional
  void deleteByGroupId(UUID groupId);
}
