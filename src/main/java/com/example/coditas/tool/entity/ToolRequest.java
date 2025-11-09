package com.example.coditas.tool.entity;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.enums.ToolNature;
import com.example.coditas.tool.enums.ToolRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "tool_request")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_number", nullable = false, unique = true, length = 50)
    private String requestNumber;

    @ManyToOne
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToolNature nature = ToolNature.FRESH;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToolRequestStatus status = ToolRequestStatus.PENDING;

    private String comment;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<ToolRequestMapping> tools;
}
