package com.example.coditas.product.entity;

import com.example.coditas.user.entity.User;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.product.enums.ProductRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "product_request")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "central_officer_id", nullable = false)
    private User centralOfficer;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductRequestStatus status = ProductRequestStatus.REQUESTED;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<ProductRequestMapping> productMappings;
}
