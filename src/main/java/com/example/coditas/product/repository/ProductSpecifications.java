package com.example.coditas.product.repository;

import com.example.coditas.product.dto.ProductFilterDto;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductCategory;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> withFilters(ProductFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("unitPrice"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("unitPrice"), filter.getMaxPrice()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getStatus()));
            }

            if (filter.getStartDate() != null && filter.getEndDate() != null) {
                ZonedDateTime start = filter.getStartDate().atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime end = filter.getEndDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());
                predicates.add(cb.between(root.get("createdAt"), start, end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> globalSearch(String query) {
        return (root, query1, cb) -> {
            if (!StringUtils.hasText(query)) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            Join<Product, ProductCategory> category = root.join("category", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(category.get("name")), like)
            );
        };
    }
}
