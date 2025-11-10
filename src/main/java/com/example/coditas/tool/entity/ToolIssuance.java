package com.example.coditas.tool.entity;

import com.example.coditas.user.entity.User;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.enums.ToolIssuanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tool_issuance")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne @JoinColumn(name = "request_id")
    private ToolRequest request;

    @ManyToOne @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne @JoinColumn(name = "issuer_id")
    private User issuer;

    @Enumerated(EnumType.STRING)
    @Column(name = "issuance_status", nullable = false)
    private ToolIssuanceStatus status = ToolIssuanceStatus.ISSUED;

    @CreatedDate
    @Column(name = "issued_at", nullable = false, updatable = false)
    private ZonedDateTime issuedAt;

    @Column(name = "returned_at")
    private ZonedDateTime returnedAt;
}
