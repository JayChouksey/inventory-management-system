package com.example.coditas.order.entity;

import com.example.coditas.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "dealer_invoice")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DealerInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false, unique = true, length = 50)
    private String invoiceId;

    @ManyToOne
    @JoinColumn(name = "dealer_id", nullable = false)
    private User dealer;

    @OneToOne
    @JoinColumn(name = "order_id")
    private DealerOrder order;

    private String url;

    @CreatedDate
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
