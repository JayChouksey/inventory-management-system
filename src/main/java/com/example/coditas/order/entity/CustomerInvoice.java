package com.example.coditas.order.entity;

import com.example.coditas.appuser.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "customer_invoice")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "invoice_id", nullable = false, unique = true, length = 50)
    private String invoiceId;

    @ManyToOne @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "order_id")
    private Integer orderId;

    private String url;

    @CreatedDate
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
