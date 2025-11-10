package com.example.coditas.centraloffice.repository;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.centraloffice.dto.CentralOfficeFilterDto;
import com.example.coditas.centraloffice.entity.CentralOffice;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CentralOfficeSpecifications {

    public static Specification<CentralOffice> withFilters(CentralOfficeFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getCity())) {
                predicates.add(cb.like(cb.lower(root.get("city")),
                        "%" + filter.getCity().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filter.getHeadName())) {
                Join<CentralOffice, User> head = root.join("head", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(head.get("name")),
                        "%" + filter.getHeadName().toLowerCase() + "%"));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CentralOffice> globalSearch(String query) {
        return (root, query1, cb) -> {
            if (!StringUtils.hasText(query)) return cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            Join<CentralOffice, User> head = root.join("head", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("city")), like),
                    cb.like(cb.lower(head.get("name")), like)
            );
        };
    }
}
