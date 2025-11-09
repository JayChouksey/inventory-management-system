package com.example.coditas.product.repository;

import com.example.coditas.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query("SELECT COALESCE(SUM(ps.quantity), 0) FROM ProductStock ps WHERE ps.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Integer productId);
}
