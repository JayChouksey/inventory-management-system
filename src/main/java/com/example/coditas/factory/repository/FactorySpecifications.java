package com.example.coditas.factory.repository;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.factory.dto.FactoryFilterDto;
import com.example.coditas.factory.entity.Factory;
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

public class FactorySpecifications {

    public static Specification<Factory> withFilters(FactoryFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getCity())) {
                predicates.add(cb.like(cb.lower(root.get("city")),
                        "%" + filter.getCity().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getPlantHeadName())) {
                Join<Factory, User> plantHead = root.join("plantHead", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(plantHead.get("name")),
                        "%" + filter.getPlantHeadName().toLowerCase() + "%"));
            }

            if (filter.getCentralOfficeId() != null) {
                predicates.add(cb.equal(root.get("centralOffice").get("id"), filter.getCentralOfficeId()));
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

    // Global search (used in /search)
    public static Specification<Factory> globalSearch(String query) {
        return (root, query1, cb) -> {
            if (!StringUtils.hasText(query)) return cb.conjunction();

            String like = "%" + query.toLowerCase() + "%";
            Join<Factory, User> plantHead = root.join("plantHead", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("city")), like),
                    cb.like(cb.lower(root.get("address")), like),
                    cb.like(cb.lower(plantHead.get("name")), like)
            );
        };
    }
}