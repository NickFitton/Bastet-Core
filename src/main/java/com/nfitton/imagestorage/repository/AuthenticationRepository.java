package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.Authentication;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, UUID> {

  Optional<Authentication> findByRandomString(String randomString);
}
