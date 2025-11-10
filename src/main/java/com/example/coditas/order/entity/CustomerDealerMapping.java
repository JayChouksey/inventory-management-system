package com.example.coditas.order.entity;

import com.example.coditas.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "customer_dealer_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dealer_id", "customer_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerDealerMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "dealer_id", nullable = false)
    private User dealer;

    @ManyToOne @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @CreatedDate
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
