package com.example.coditas.tool.repository;

import com.example.coditas.tool.entity.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Long> {
}
