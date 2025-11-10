package com.example.coditas.product.repository;

import com.example.coditas.common.dto.CategoryFilterDto;
import com.example.coditas.product.entity.ProductCategory;
import com.example.coditas.tool.entity.ToolCategory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ProductCategorySpecifications {
    public static Specification<ProductCategory> withFilters(CategoryFilterDto filter) {
        return (root, query, cb) -> {
            if (StringUtils.hasText(filter.getName())) {
                return cb.like(cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%");
            }
            return cb.conjunction();
        };
    }

    public static Specification<ProductCategory> globalSearch(String query) {
        return (root, query1, cb) -> {
            if (!StringUtils.hasText(query)) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), like);
        };
    }
}
