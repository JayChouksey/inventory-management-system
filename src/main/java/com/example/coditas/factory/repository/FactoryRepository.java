package com.example.coditas.factory.repository;

import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.factory.entity.Factory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {

    long countByIsActive(ActiveStatus isActive);
    List<Factory> findAllByIsActive(ActiveStatus isActive);
    Optional<Factory> findByFactoryId(String id);
    Optional<Factory> findActiveByFactoryId(String id);
    Optional<Factory> findById(Long id);

    Page<Factory> findAll(Specification<Factory> spec, Pageable pageable);
}
