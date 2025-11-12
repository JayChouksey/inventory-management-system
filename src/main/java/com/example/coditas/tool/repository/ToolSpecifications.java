package com.example.coditas.tool.repository;

import com.example.coditas.tool.dto.ToolFilterDto;
import com.example.coditas.tool.entity.Tool;
import com.example.coditas.tool.entity.ToolCategory;
import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
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

public class ToolSpecifications {

    public static Specification<Tool> withFilters(ToolFilterDto filter) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getPerishable() != null) {
                Perishable isPerishable = Perishable.getType(filter.getPerishable().toUpperCase());
                predicates.add(cb.equal(root.get("isPerishable"), isPerishable));
            }
            if (filter.getExpensive() != null) {
                Expensive isExpensive = Expensive.getType(filter.getExpensive().toUpperCase());
                predicates.add(cb.equal(root.get("isExpensive"), isExpensive));
            }
            if (filter.getStartDate() != null && filter.getEndDate() != null) {
                ZonedDateTime start = filter.getStartDate().atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime end = filter.getEndDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());
                predicates.add(cb.between(root.get("createdAt"), start, end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Tool> globalSearch(String query) {
        return (root, query1, cb) -> {
            if (!StringUtils.hasText(query)) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            Join<Tool, ToolCategory> cat = root.join("category", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(cat.get("name")), like)
            );
        };
    }
}
