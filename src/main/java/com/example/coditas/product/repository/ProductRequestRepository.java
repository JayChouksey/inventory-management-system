package com.example.coditas.product.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.product.entity.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRequestRepository extends JpaRepository<ProductRequest, Long> {
    Page<ProductRequest> findByFactory(Factory factory, Pageable pageable);
}
