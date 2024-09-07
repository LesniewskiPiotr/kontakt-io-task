package com.example.kontaktiotask.repository;

import com.example.kontaktiotask.model.Asset;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.id = :id")
    Optional<Asset> findByIdWithLock(Long id);
    @Query("SELECT a FROM Asset a LEFT JOIN FETCH a.groups WHERE a.id = :id")
    Optional<Asset> findByIdWithGroups(Long id);
}
