package com.nfitton.imagestorage.repository;

import com.nfitton.imagestorage.entity.Account;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
}
