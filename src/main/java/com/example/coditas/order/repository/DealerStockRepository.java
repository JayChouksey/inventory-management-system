package com.example.coditas.order.repository;

import com.example.coditas.order.entity.DealerStock;
import com.example.coditas.product.entity.Product;
import com.example.coditas.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DealerStockRepository extends JpaRepository<DealerStock, Long> {
    Optional<DealerStock> findByDealerAndProduct(User dealer, Product product);
}
