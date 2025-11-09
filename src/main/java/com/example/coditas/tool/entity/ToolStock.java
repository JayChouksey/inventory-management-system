package com.example.coditas.tool.entity;

import com.example.coditas.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tool_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"factory_id", "tool_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "issued_quantity", nullable = false)
    private Integer issuedQuantity = 0;

    @LastModifiedDate
    @Column(name = "last_updated_at", nullable = false)
    private ZonedDateTime lastUpdatedAt;
}
