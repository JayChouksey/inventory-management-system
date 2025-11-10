package com.example.coditas.tool.repository;

import com.example.coditas.tool.entity.ToolCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT COUNT(t) FROM Tool t WHERE t.category.id = :categoryId")
    Long countToolsByCategoryId(@Param("categoryId") Long categoryId);

    Page<ToolCategory> findAll(Specification<ToolCategory> spec, Pageable pageable);
}
