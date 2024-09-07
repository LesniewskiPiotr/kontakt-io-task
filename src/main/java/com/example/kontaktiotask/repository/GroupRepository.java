package com.example.kontaktiotask.repository;

import com.example.kontaktiotask.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.assets WHERE g.id = :id")
    Optional<Group> findByIdWithAssets(Long id);
}
