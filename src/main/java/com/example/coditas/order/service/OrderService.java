package com.example.coditas.order.service;

import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.repository.FactoryRepository;
import com.example.coditas.order.dto.*;
import com.example.coditas.order.entity.DealerInvoice;
import com.example.coditas.order.entity.DealerOrder;
import com.example.coditas.order.entity.DealerOrderMapping;
import com.example.coditas.order.entity.DealerStock;
import com.example.coditas.order.enums.DealerOrderStatus;
import com.example.coditas.order.repository.DealerInvoiceRepository;
import com.example.coditas.order.repository.DealerOrderRepository;
import com.example.coditas.order.repository.DealerStockRepository;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductStock;
import com.example.coditas.product.repository.ProductRepository;
import com.example.coditas.product.repository.ProductStockRepository;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final DealerOrderRepository dealerOrderRepository;
    private final DealerInvoiceRepository dealerInvoiceRepository;
    private final DealerStockRepository dealerStockRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final FactoryRepository factoryRepository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;

    // ──────────────────────────────────────────────────────────────
    // CREATE DEALER ORDER (DEALER)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public DealerOrderResponseDto createDealerOrder(DealerOrderCreateDto dto) {
        User dealer = getCurrentDealer();

        String orderId = generateOrderId();
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<DealerOrderMapping> mappings = new ArrayList<>();
        List<OrderItemDto> items = new ArrayList<>();

        for (OrderItemDto itemDto : dto.getProducts()) {
            Product product = productRepository.findByProductId(itemDto.getProductId())
                    .orElseThrow(() -> new CustomException("Product not found: " + itemDto.getProductId(), HttpStatus.NOT_FOUND));

            BigDecimal qty = BigDecimal.valueOf(itemDto.getQuantity());
            BigDecimal itemPrice = product.getUnitPrice().multiply(qty);
            totalPrice = totalPrice.add(itemPrice);

            DealerOrderMapping mapping = DealerOrderMapping.builder()
                    .order(null) // will be set after save
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .build();
            mappings.add(mapping);

            items.add(OrderItemDto.builder()
                    .productId(product.getProductId())
//                    .productName(product.getName())
                    .quantity(itemDto.getQuantity())
//                    .unitPrice(product.getUnitPrice())
                    .build());
        }

        DealerOrder order = DealerOrder.builder()
                .orderId(orderId)
                .dealer(dealer)
                .status(DealerOrderStatus.PENDING)
                .comment(dto.getComment())
                .totalPrice(totalPrice)
                .items(mappings)
                .build();

        // Link back
        mappings.forEach(m -> m.setOrder(order));

        DealerOrder savedOrder = dealerOrderRepository.save(order);

        log.info("Dealer order created: {} by {}", orderId, dealer.getName());
        return toResponseDto(savedOrder, items);
    }

    // ──────────────────────────────────────────────────────────────
    // FULFILL ORDER (CENTRAL OFFICE)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public DealerInvoiceResponseDto fulfillDealerOrder(String orderId, FulfillOrderDto dto) {
        User centralOfficer = getCurrentCentralOfficer();

        DealerOrder order = dealerOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Order not found", HttpStatus.NOT_FOUND));

        // TODO: Check while fetching
        if (order.getStatus() != DealerOrderStatus.PENDING) {
            throw new CustomException("Order already processed: " + order.getStatus(), HttpStatus.CONFLICT);
        }

        Factory factory = factoryRepository.findByFactoryId(dto.getFactoryId())
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        User dealer = order.getDealer();

        for (DealerOrderMapping mapping : order.getItems()) {
            Product product = mapping.getProduct();
            Long qty = mapping.getQuantity();

            // Deduct from Factory
            ProductStock factoryStock = productStockRepository.findByFactoryAndProduct(factory, product)
                    .orElseThrow(() -> new CustomException(
                            "No stock for " + product.getName() + " in factory " + factory.getName(),
                            HttpStatus.CONFLICT));

            // TODO: Merge the check while fetching above
            if (factoryStock.getQuantity() < qty) {
                throw new CustomException(
                        "Insufficient stock: " + product.getName() + " (Need: " + qty + ", Avail: " + factoryStock.getQuantity() + ")",
                        HttpStatus.CONFLICT);
            }

            factoryStock.setQuantity(factoryStock.getQuantity() - qty);
            productStockRepository.save(factoryStock);

            // Add to Dealer
            DealerStock dealerStock = dealerStockRepository.findByDealerAndProduct(dealer, product)
                    .orElseGet(() -> DealerStock.builder()
                            .dealer(dealer)
                            .product(product)
                            .quantity(0L)
                            .build());

            dealerStock.setQuantity(dealerStock.getQuantity() + qty);
            dealerStockRepository.save(dealerStock);
        }

        order.setStatus(DealerOrderStatus.APPROVED);
        order.setUpdatedBy(centralOfficer);
        dealerOrderRepository.save(order);

        String invoiceId = generateInvoiceId();
        DealerInvoice invoice = DealerInvoice.builder()
                .invoiceId(invoiceId)
                .dealer(dealer)
                .order(order)
                .url("/invoices/dealer/" + orderId + ".pdf")
                .build();

        // TODO
        DealerInvoice savedInvoice = dealerInvoiceRepository.saveAndFlush(invoice);
        entityManager.refresh(savedInvoice);


        log.info("Order FULFILLED: {} from factory {}", orderId, factory.getName());
        return toInvoiceDto(savedInvoice);
    }

    // ──────────────────────────────────────────────────────────────
    // GET ALL ORDERS
    // ──────────────────────────────────────────────────────────────
    public Page<DealerOrderResponseDto> getAllDealerOrders(PageableDto pageReq) {
        Pageable pageable = toPageable(pageReq);
        Page<DealerOrder> page = dealerOrderRepository.findAll(pageable);
        return page.map(o -> toResponseDto(o, mapItems(o.getItems())));
    }

    // ──────────────────────────────────────────────────────────────
    // GET ORDER BY ID
    // ──────────────────────────────────────────────────────────────
    public DealerOrderResponseDto getDealerOrderById(String orderId) {
        DealerOrder order = dealerOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Order not found", HttpStatus.NOT_FOUND));
        return toResponseDto(order, mapItems(order.getItems()));
    }

    // TODO:
    // ──────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private User getCurrentDealer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));
        if (!user.getRole().getName().equals(UserRole.DEALER)) {
            throw new CustomException("Only Dealers can place orders", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private User getCurrentCentralOfficer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));
        if (!List.of(UserRole.CENTRAL_OFFICE_HEAD, UserRole.CENTRAL_OFFICE_HEAD).contains(user.getRole().getName())) {
            throw new CustomException("Only Central Office can fulfill orders", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private String generateOrderId() {
        long count = dealerOrderRepository.count();
        return String.format("ORD-%06d", count + 1);
    }

    private String generateInvoiceId() {
        long count = dealerInvoiceRepository.count();
        return String.format("INV-%06d", count + 1);
    }

    private Pageable toPageable(PageableDto dto) {
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private List<OrderItemDto> mapItems(List<DealerOrderMapping> mappings) {
        return mappings.stream().map(m -> {
            Product p = m.getProduct();
            return OrderItemDto.builder()
                    .productId(p.getProductId())
//                    .productName(p.getName())
                    .quantity(m.getQuantity())
//                    .unitPrice(m.getUnitPrice())
                    .build();
        }).toList();
    }

    private DealerOrderResponseDto toResponseDto(DealerOrder o, List<OrderItemDto> items) {
        String updatedBy = o.getUpdatedBy() != null ? o.getUpdatedBy().getName() : null;

        return DealerOrderResponseDto.builder()
                .orderId(o.getOrderId())
                .dealerId(o.getDealer().getUserId())
                .dealerName(o.getDealer().getName())
                .totalPrice(o.getTotalPrice())
                .status(o.getStatus())
                .comment(o.getComment())
                .createdAt(o.getCreatedAt())
                .products(items)
                .build();
    }

    private DealerInvoiceResponseDto toInvoiceDto(DealerInvoice i) {
        return DealerInvoiceResponseDto.builder()
                .invoiceId(i.getInvoiceId())
                .orderId(i.getOrder().getOrderId())
                .dealerId(i.getDealer().getUserId())
                .dealerName(i.getDealer().getName())
                .pdfUrl(i.getUrl())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
