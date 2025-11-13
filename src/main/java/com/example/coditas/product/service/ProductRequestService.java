package com.example.coditas.product.service;

import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.repository.FactoryRepository;
import com.example.coditas.product.dto.ProductRequestCreateDto;
import com.example.coditas.product.dto.ProductRequestItemDto;
import com.example.coditas.product.dto.ProductRequestResponseDto;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductRequest;
import com.example.coditas.product.entity.ProductRequestMapping;
import com.example.coditas.product.enums.ProductRequestStatus;
import com.example.coditas.product.repository.ProductRepository;
import com.example.coditas.product.repository.ProductRequestMappingRepository;
import com.example.coditas.product.repository.ProductRequestRepository;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductRequestService {

    private final UserRepository userRepository;
    private final ProductRequestRepository productRequestRepository;
    private final ProductRequestMappingRepository productRequestMappingRepository;
    private final ProductRepository productRepository;
    private final FactoryRepository factoryRepository;
    private final EntityManager entityManager;

    // ──────────────────────────────────────────────────────────────
    // CREATE PRODUCT REQUEST (CENTRAL OFFICE)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ProductRequestResponseDto createProductRequest(ProductRequestCreateDto dto) {
        User centralOfficer = getCurrentCentralOfficer();

        Factory factory = factoryRepository.findByFactoryId(dto.getFactoryId())
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        String requestNumber = generateRequestNumber();

        ProductRequest request = ProductRequest.builder()
                .centralOfficer(centralOfficer)
                .factory(factory)
                .status(ProductRequestStatus.REQUESTED)
                .build();

        ProductRequest savedRequest = productRequestRepository.saveAndFlush(request);
        entityManager.refresh(savedRequest);

        List<ProductRequestMapping> mappings = new ArrayList<>();
        List<ProductRequestItemDto> items = new ArrayList<>();

        for (ProductRequestItemDto itemDto : dto.getProducts()) {
            Product product = productRepository.findByProductId(itemDto.getProductId())
                    .orElseThrow(() -> new CustomException("Product not found: " + itemDto.getProductId(), HttpStatus.NOT_FOUND));

            ProductRequestMapping mapping = ProductRequestMapping.builder()
                    .request(savedRequest)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .build();

            mappings.add(mapping);

            items.add(ProductRequestItemDto.builder()
                    .productId(product.getProductId())
                    .quantity(itemDto.getQuantity())
                    .build());
        }

        productRequestMappingRepository.saveAllAndFlush(mappings);
        savedRequest.getProductMappings().addAll(mappings);


        log.info("Product request created: {} for factory {}", requestNumber, factory.getName());
        return toResponseDto(savedRequest, items);
    }

    // ──────────────────────────────────────────────────────────────
    // APPROVE REQUEST (PLANT HEAD)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ProductRequestResponseDto approveRequest(Long requestId) {
        ProductRequest request = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException("Product request not found", HttpStatus.NOT_FOUND));

        if (request.getStatus() != ProductRequestStatus.REQUESTED) {
            throw new CustomException("Only REQUESTED requests can be approved", HttpStatus.BAD_REQUEST);
        }

        request.setStatus(ProductRequestStatus.FULFILLED);
        productRequestRepository.save(request);

        log.info("Product request APPROVED by Plant Head of {}", request.getFactory().getName());
        return toResponseDto(request, mapItems(request.getProductMappings()));
    }

    // ──────────────────────────────────────────────────────────────
    // REJECT REQUEST (PLANT HEAD)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ProductRequestResponseDto rejectRequest(Long requestId) {
        ProductRequest request = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException("Product request not found", HttpStatus.NOT_FOUND));

        if (request.getStatus() != ProductRequestStatus.REQUESTED) {
            throw new CustomException("Only REQUESTED requests can be rejected", HttpStatus.BAD_REQUEST);
        }

        request.setStatus(ProductRequestStatus.REJECTED);
        productRequestRepository.save(request);

        log.info("Product request REJECTED by Plant Head of {}", request.getFactory().getName());

        return toResponseDto(request, mapItems(request.getProductMappings()));
    }

    // ──────────────────────────────────────────────────────────────
    // GET ALL REQUESTS (ADMIN / CENTRAL OFFICE)
    // ──────────────────────────────────────────────────────────────
    public Page<ProductRequestResponseDto> getAllProductRequests(PageableDto pageReq) {
        Pageable pageable = toPageable(pageReq);
        Page<ProductRequest> page = productRequestRepository.findAll(pageable);
        return page.map(r -> toResponseDto(r, mapItems(r.getProductMappings())));
    }

    // ──────────────────────────────────────────────────────────────
    // GET BY FACTORY (PLANT HEAD)
    // ──────────────────────────────────────────────────────────────
    public Page<ProductRequestResponseDto> getProductRequestsByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        Pageable pageable = toPageable(pageReq);
        Page<ProductRequest> page = productRequestRepository.findByFactory(factory, pageable);
        return page.map(r -> toResponseDto(r, mapItems(r.getProductMappings())));
    }

    // ──────────────────────────────────────────────────────────────
    // GET BY ID
    // ──────────────────────────────────────────────────────────────
    public ProductRequestResponseDto getProductRequestById(Long requestId) {
        ProductRequest request = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException("Product request not found", HttpStatus.NOT_FOUND));
        return toResponseDto(request, mapItems(request.getProductMappings()));
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private User getCurrentCentralOfficer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));

        if (user.getRole() == null || !user.getRole().getName().equals(UserRole.CENTRAL_OFFICE_HEAD)) {
            throw new CustomException("Only Central Office can create product requests", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private String generateRequestNumber() {
        long count = productRequestRepository.count();
        return String.format("PREQ-%06d", count + 1);
    }

    private Pageable toPageable(PageableDto dto) {
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private List<ProductRequestItemDto> mapItems(List<ProductRequestMapping> mappings) {
        return mappings.stream().map(m -> {
            Product p = m.getProduct();
            return ProductRequestItemDto.builder()
                    .productId(p.getProductId())
                    .quantity(m.getQuantity())
                    .build();
        }).toList();
    }

    private ProductRequestResponseDto toResponseDto(ProductRequest r, List<ProductRequestItemDto> items) {
        return ProductRequestResponseDto.builder()
                .id(r.getId())
                .centralOfficerName(r.getCentralOfficer().getName())
                .factoryId(r.getFactory().getFactoryId())
                .factoryName(r.getFactory().getName())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .products(items)
                .build();
    }
}
