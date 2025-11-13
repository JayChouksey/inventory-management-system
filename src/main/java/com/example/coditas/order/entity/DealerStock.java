package com.example.coditas.order.entity;

import com.example.coditas.product.entity.Product;
import com.example.coditas.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "dealer_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dealer_id", "product_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealerStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false, referencedColumnName = "user_id")
    private User dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Column(nullable = false)
    private Long quantity = 0L;
}