package com.example.coditas.appuser.repository;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.factory.entity.UserFactoryMapping;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withFilters(
            String name,
            String email,
            String phone,
            String roleId,
            String factoryId,
            String bayId,
            ActiveStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Name - case insensitive
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }

            // Email - case insensitive
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"));
            }

            // Phone - partial match
            if (phone != null && !phone.isBlank()) {
                predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
            }

            // Role
            if (roleId != null && !roleId.isBlank()) {
                predicates.add(cb.equal(root.get("role").get("id"), roleId));
            }

            // Factory mapping
            if (factoryId != null && !factoryId.isBlank()) {
                Join<Object, Object> mapping = root.join("userFactoryMappings", JoinType.INNER);
                predicates.add(cb.equal(mapping.get("factory").get("id"), factoryId));
            }

            // Bay mapping
            if (bayId != null && !bayId.isBlank()) {
                Join<Object, Object> mapping = root.join("userFactoryMappings", JoinType.INNER);
                predicates.add(cb.equal(mapping.get("bay").get("id"), bayId));
            }

            // Status
            if (status != null) {
                predicates.add(cb.equal(root.get("isActive"), status));
            }

            // Avoid duplicates due to joins
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> globalSearch(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.isTrue(cb.literal(true)); // Return all if query is empty
            }

            String likePattern = "%" + query.toLowerCase() + "%";

            Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern);
            Predicate emailPredicate = cb.like(cb.lower(root.get("email")), likePattern);
            Predicate phonePredicate = cb.like(cb.lower(root.get("phone")), likePattern);
            Predicate userIdPredicate = cb.like(cb.lower(root.get("userId")), likePattern);

            Predicate activePredicate = cb.equal(root.get("isActive"), ActiveStatus.ACTIVE);

            return cb.and(
                    activePredicate,
                    cb.or(namePredicate, emailPredicate, phonePredicate, userIdPredicate)
            );
        };
    }

    // FACTORY WORKERS (PLANT HEAD)
    public static Specification<User> factoryWorkers(String factoryId) {
        return (root, query, cb) -> {
            Join<User, UserFactoryMapping> mapping = root.join("factoryMappings", JoinType.INNER);

            query.distinct(true);

            return cb.and(
                    cb.equal(mapping.get("factory").get("id"), factoryId),
                    root.get("role").get("name").in("CHIEF_SUPERVISOR", "WORKER"),
                    cb.equal(root.get("isActive"), ActiveStatus.ACTIVE)
            );
        };
    }

    // BAY WORKERS (CHIEF SUPERVISOR)
    public static Specification<User> bayWorkers(String bayId) {
        return (root, query, cb) -> {
            Join<User, UserFactoryMapping> mapping = root.join("factoryMappings", JoinType.INNER);

            query.distinct(true);

            return cb.and(
                    cb.equal(mapping.get("bay").get("id"), bayId),
                    cb.equal(root.get("role").get("name"), "WORKER"),
                    cb.equal(root.get("isActive"), ActiveStatus.ACTIVE)
            );
        };
    }
}
