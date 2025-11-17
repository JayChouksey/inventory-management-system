package com.example.coditas.product.service;

import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.entity.UserFactoryMapping;
import com.example.coditas.factory.repository.FactoryRepository;
import com.example.coditas.product.dto.FactoryProductionResponseDto;
import com.example.coditas.product.dto.ProductStockResponseDto;
import com.example.coditas.product.dto.RecordProductionRequestDto;
import com.example.coditas.product.entity.FactoryProduction;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductStock;
import com.example.coditas.product.repository.FactoryProductionRepository;
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
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FactoryProductionService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final FactoryProductionRepository factoryProductionRepository;
    private final FactoryRepository factoryRepository;
    private final EntityManager entityManager;

    // ──────────────────────────────────────────────────────────────
    // RECORD PRODUCTION (PLANT HEAD)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public FactoryProductionResponseDto recordProduction(RecordProductionRequestDto dto) {
        User plantHead = getCurrentPlantHead();
        Factory factory = getFactoryForUser(plantHead);

        Product product = productRepository.findByProductId(dto.getProductId())
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));

        FactoryProduction production = FactoryProduction.builder()
                .factory(factory)
                .product(product)
                .productionQuantity(dto.getProductionQuantity())
                .productionDate(dto.getProductionDate())
                .build();

        production = factoryProductionRepository.save(production);

        // Update stock
        ProductStock stock = productStockRepository.findByFactoryAndProduct(factory, product)
                .orElseGet(() -> ProductStock.builder()
                        .factory(factory)
                        .product(product)
                        .quantity(0L)
                        .build());

        stock.setQuantity(stock.getQuantity() + dto.getProductionQuantity());
        productStockRepository.save(stock);

        log.info("Production recorded: {} x{} in factory {}",
                product.getName(), dto.getProductionQuantity(), factory.getName());

        return toProductionDto(production);
    }

    // TODO: Combine if time permits
    // ──────────────────────────────────────────────────────────────
    // GET STOCK BY FACTORY
    // ──────────────────────────────────────────────────────────────
    public Page<ProductStockResponseDto> getStockByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        Pageable pageable = toPageable(pageReq, "product.name");
        Page<ProductStock> page = productStockRepository.findByFactory(factory, pageable);
        return page.map(this::toStockDto);
    }

    // ──────────────────────────────────────────────────────────────
    // GET PRODUCTION RECORDS BY FACTORY
    // ──────────────────────────────────────────────────────────────
    public Page<FactoryProductionResponseDto> getProductionRecordsByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        Pageable pageable = toPageable(pageReq, "productionDate", "desc");
        Page<FactoryProduction> page = factoryProductionRepository.findByFactory(factory, pageable);
        return page.map(this::toProductionDto);
    }

    // ──────────────────────────────────────────────────────────────
    // GET STOCK LEVELS FOR PRODUCT ACROSS ALL FACTORIES (CENTRAL OFFICE)
    // ──────────────────────────────────────────────────────────────
    public Page<ProductStockResponseDto> getStockLevelsForProduct(String productId, PageableDto pageReq) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));

        Pageable pageable = toPageable(pageReq, "quantity", "desc");
        Page<ProductStock> page = productStockRepository.findByProduct(product, pageable);
        return page.map(this::toStockDto);
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private User getCurrentPlantHead() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));

        if (user.getRole() == null || !user.getRole().getName().equals(UserRole.PLANT_HEAD)) {
            throw new CustomException("Only Plant Head can perform this action", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private Factory getFactoryForUser(User user) {
        return user.getFactoryMappings().stream()
                .map(UserFactoryMapping::getFactory)
                .findFirst()
                .orElseThrow(() -> new CustomException("User not assigned to any factory", HttpStatus.BAD_REQUEST));
    }

    private Pageable toPageable(PageableDto dto, String defaultField) {
        return toPageable(dto, defaultField, "asc");
    }

    private Pageable toPageable(PageableDto dto, String defaultField, String defaultDir) {
        String field = StringUtils.hasText(dto.getSortBy()) ? dto.getSortBy() : defaultField;
        String dir = StringUtils.hasText(dto.getSortDir()) ? dto.getSortDir() : defaultDir;

        Sort sort = "desc".equalsIgnoreCase(dir)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();

        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private ProductStockResponseDto toStockDto(ProductStock s) {
        return ProductStockResponseDto.builder()
                .productId(s.getProduct().getProductId())
                .productName(s.getProduct().getName())
                .factoryId(s.getFactory().getFactoryId())
                .factoryName(s.getFactory().getName())
                .quantity(s.getQuantity())
                .build();
    }

    private FactoryProductionResponseDto toProductionDto(FactoryProduction p) {
        return FactoryProductionResponseDto.builder()
                .id(p.getId())
                .productId(p.getProduct().getProductId())
                .productName(p.getProduct().getName())
                .factoryId(p.getFactory().getFactoryId())
                .factoryName(p.getFactory().getName())
                .productionQuantity(p.getProductionQuantity())
                .productionDate(p.getProductionDate())
                .productionDate(p.getProductionDate())
                .build();
    }
}