package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
