package com.example.coditas.tool.repository;

import com.example.coditas.tool.entity.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {
    Page<Tool> findAll(Specification<Tool> spec, Pageable pageable);

    Optional<Tool> findByToolId(String id);

    boolean existsByNameIgnoreCase(String trim);
}
