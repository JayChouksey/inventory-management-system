package com.example.coditas.product.service;

import com.example.coditas.common.dto.GenericFilterDto;
import com.example.coditas.common.specification.GenericFilterSpecFactory;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.UserRepository;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.util.CloudinaryService;
import com.example.coditas.product.dto.ProductCreateRequestDto;
import com.example.coditas.product.dto.ProductResponseDto;
import com.example.coditas.product.dto.ProductUpdateRequestDto;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductCategory;
import com.example.coditas.product.repository.ProductCategoryRepository;
import com.example.coditas.product.repository.ProductRepository;
import com.example.coditas.product.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    public Page<ProductResponseDto> searchProducts(GenericFilterDto filter, PageableDto pageReq) {
        Specification<Product> spec = GenericFilterSpecFactory.forProduct(filter);
        Pageable pageable = toPageableForProduct(pageReq);
        Page<Product> page = productRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<ProductResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<Product> spec = GenericFilterSpecFactory.globalSearch(
                new GenericFilterDto(){{setName(q);}}, "name"
        );
        Pageable pageable = toPageableForProduct(pageReq);
        Page<Product> page = productRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public ProductResponseDto getProductDetail(String id) {
        Product product = productRepository.findActiveByProductId(id)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));
        return toDto(product);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto dto) {
        ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));

        String imageUrl = cloudinaryService.uploadFile(dto.getImage());
        String productId = generateUniqueProductId();

        Product product = Product.builder()
                .productId(productId)
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .imageUrl(imageUrl)
                .unitPrice(dto.getUnitPrice())
                .category(category)
                .isActive(ActiveStatus.ACTIVE)
                .createdBy(getCurrentUser())
                .build();

        product = productRepository.save(product);

        log.info("Product created: {} ({})", product.getName(), product.getProductId());
        return toDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(String id, ProductUpdateRequestDto dto) {
        Product product = productRepository.findActiveByProductId(id)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            product.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getUnitPrice() != null && dto.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setUnitPrice(dto.getUnitPrice());
        }
        if (dto.getCategoryId() != null) {
            ProductCategory cat = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));
            product.setCategory(cat);
        }
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            if (product.getImageUrl() != null) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(product.getImageUrl());
                cloudinaryService.deleteFile(publicId);
            }
            product.setImageUrl(cloudinaryService.uploadFile(dto.getImage()));
        }

        product.setUpdatedBy(getCurrentUser());
        product.setUpdatedAt(ZonedDateTime.now());
        product = productRepository.save(product);

        return toDto(product);
    }

    @Transactional
    public String softDeleteProduct(String id) {
        Product product = productRepository.findByProductId(id)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));
        if (product.getIsActive() == ActiveStatus.INACTIVE) {
            throw new CustomException("Product already deleted", HttpStatus.CONFLICT);
        }
        product.setIsActive(ActiveStatus.INACTIVE);
        productRepository.save(product);
        return "Product deleted successfully!";
    }

    private String generateUniqueProductId() {
        long count = productRepository.count();
        return String.format("PROD-%04d", count + 1);
    }

    private Pageable toPageableForProduct(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "name" -> "name";
            case "price", "unit_price" -> "unitPrice";
            case "category" -> "category.name";
            case "created_on", "createdat" -> "createdAt";
            default -> "createdAt";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private ProductResponseDto toDto(Product p) {
        Integer totalStock = productStockRepository.sumQuantityByProductId(p.getId());
        if (totalStock == null) totalStock = 0;

        String stockStatus = totalStock > 50 ? "IN_STOCK"
                : totalStock > 0 ? "LOW"
                : "OUT_OF_STOCK";

        return ProductResponseDto.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .description(p.getDescription())
                .imageUrl(p.getImageUrl())
                .unitPrice(p.getUnitPrice())
                .categoryName(p.getCategory().getName())
                .status(p.getIsActive().name())
                .createdOn(p.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .currentStock(totalStock)
                .stockStatus(stockStatus)
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail("admin@company.com") // TODO: Change it once JWT is implemented
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }
}
