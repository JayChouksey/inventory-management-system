package com.example.coditas.common.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class GenericSpecificationBuilder<T> {

    private final List<SpecPart<T>> parts = new ArrayList<>();

    private GenericSpecificationBuilder() {}

    public static <T> GenericSpecificationBuilder<T> builder() {
        return new GenericSpecificationBuilder<>();
    }

    public GenericSpecificationBuilder<T> add(SpecPart<T> part) {
        this.parts.add(part);
        return this;
    }

    public Specification<T> build() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean hasJoin = false;

            for (SpecPart<T> part : parts) {
                Predicate p = part.apply(root, cb);
                if (p != null) {
                    predicates.add(p);
                    if (part.isJoin()) hasJoin = true;
                }
            }

            if (hasJoin) query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @FunctionalInterface
    public interface SpecPart<U> {
        Predicate apply(Root<U> root, CriteriaBuilder cb);
        default boolean isJoin() { return false; }
    }
}
