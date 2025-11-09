package com.example.coditas.order.entity;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.centraloffice.entity.CentralOffice;
import com.example.coditas.order.enums.DealerOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "dealer_order")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DealerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @ManyToOne @JoinColumn(name = "dealer_id", nullable = false)
    private User dealer;

    @ManyToOne @JoinColumn(name = "central_office_id")
    private CentralOffice centralOffice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DealerOrderStatus status = DealerOrderStatus.PENDING;

    private String comment;

    @ManyToOne @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<DealerOrderMapping> items;
}
