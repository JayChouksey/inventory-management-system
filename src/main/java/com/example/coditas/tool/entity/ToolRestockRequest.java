package com.example.coditas.tool.entity;

import com.example.coditas.user.entity.User;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.enums.RestockStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "tool_restock_request")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolRestockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne @JoinColumn(name = "requested_by")
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestockStatus status = RestockStatus.PENDING;

    @ManyToOne @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<ToolRestockRequestMapping> tools;
}