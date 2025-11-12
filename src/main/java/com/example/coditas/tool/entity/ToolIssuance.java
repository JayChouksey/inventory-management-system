package com.example.coditas.tool.entity;

import com.example.coditas.user.entity.User;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.enums.ToolIssuanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "tool_issuance")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne @JoinColumn(name = "request_id")
    private ToolRequest request;

    @ManyToOne @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne @JoinColumn(name = "issuer_id")
    private User issuer;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "issuance_status", nullable = false)
    private ToolIssuanceStatus status = ToolIssuanceStatus.ISSUED;


    // TODO: Change ZoneDateTime to LocalDateTime
    @CreatedDate
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @PrePersist
    protected void onCreate(){
        issuedAt = LocalDateTime.now();
    }

}
