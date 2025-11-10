package com.example.coditas.tool.entity;

import com.example.coditas.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tool_return")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne @JoinColumn(name = "tool_issuance_id", nullable = false)
    private ToolIssuance issuance;

    @Column(name = "fit_quantity", nullable = false)
    private Integer fitQuantity = 0;

    @Column(name = "unfit_quantity", nullable = false)
    private Integer unfitQuantity = 0;

    @ManyToOne @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "returned_at", nullable = false)
    private ZonedDateTime returnedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @CreatedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
