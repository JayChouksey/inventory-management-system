package com.example.coditas.order.entity;

import com.example.coditas.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "dealer_order_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DealerOrderMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "order_id", nullable = false)
    private DealerOrder order;

    @ManyToOne @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long quantity = 0L;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;
}
