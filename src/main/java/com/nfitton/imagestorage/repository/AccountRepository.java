package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<User, UUID> {

  @Query("SELECT id FROM User")
  List<UUID> findAllAccount();

  Optional<User> findByEmail(String email);
}
