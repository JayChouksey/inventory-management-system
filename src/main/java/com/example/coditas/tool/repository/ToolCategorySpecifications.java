package com.example.coditas.tool.repository;

import com.example.coditas.common.dto.CategoryFilterDto;
import com.example.coditas.tool.entity.ToolCategory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ToolCategorySpecifications {

    public static Specification<ToolCategory> withFilters(CategoryFilterDto filter) {
        return (root, query, cb) -> {
            if (StringUtils.hasText(filter.getName())) {
                return cb.like(cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%");
            }
            return cb.conjunction();
        };
    }

    public static Specification<ToolCategory> globalSearch(String query) {
        return (root, query1, cb) -> {
            if (!StringUtils.hasText(query)) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), like);
        };
    }
}
