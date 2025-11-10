package com.example.coditas.product.repository;

import com.example.coditas.product.entity.ProductCategory;
import com.example.coditas.tool.entity.ToolCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query("SELECT COALESCE(SUM(ps.quantity), 0) FROM ProductStock ps WHERE ps.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Integer productId);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<ProductCategory> findAll(Specification<ProductCategory> spec, Pageable pageable);
}
