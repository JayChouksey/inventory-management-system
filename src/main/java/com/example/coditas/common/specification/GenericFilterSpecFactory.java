package com.example.coditas.common.specification;

import com.example.coditas.centraloffice.entity.CentralOffice;
import com.example.coditas.common.dto.GenericFilterDto;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductCategory;
import com.example.coditas.tool.entity.Tool;
import com.example.coditas.tool.entity.ToolCategory;
import com.example.coditas.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GenericFilterSpecFactory {

    private GenericFilterSpecFactory() {}

    // CENTRAL OFFICE
    public static Specification<CentralOffice> forCentralOffice(GenericFilterDto filter) {
        ActiveStatus status = ActiveStatus.ACTIVE;
        if(filter.getStatus() != null){
            status = ActiveStatus.getType(filter.getStatus().toUpperCase());
        }
        return GenericSpecificationBuilder.<CentralOffice>builder()
                .add(like("city", filter.getCity()))
                .add(joinLike("head", "name", filter.getPlantHeadName()))
                .add(equal("isActive", status))
                .build();
    }

    // FACTORY
    public static Specification<Factory> forFactory(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<Factory>builder()
                .add(like("name", filter.getName()))
                .add(like("city", filter.getCity()))
                .add(joinLike("plantHead", "name", filter.getPlantHeadName()))
                .add(joinEqual("centralOffice", "id", filter.getCentralOfficeId()))
                .add(equal("isActive", filter.getStatus()))
                .add(between("createdAt", filter.getStartDate(), filter.getEndDate()))
                .build();
    }

    // PRODUCT
    public static Specification<Product> forProduct(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<Product>builder()
                .add(like("name", filter.getName()))
                .add(joinEqual("category", "id", filter.getCategoryId()))
                .add(greaterThanEqual("unitPrice", filter.getMinPrice()))
                .add(lessThanEqual("unitPrice", filter.getMaxPrice()))
                .add(equal("isActive", filter.getStatus()))
                .add(between("createdAt", filter.getStartDate(), filter.getEndDate()))
                .build();
    }

    // USER
    public static Specification<User> forUser(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<User>builder()
                .add(like("name", filter.getName()))
                .add(like("email", filter.getEmail()))
                .add(like("phone", filter.getPhone()))
                .add(joinEqual("role", "id", filter.getRoleId()))
                .add(equal("isActive", filter.getStatus()))
                .add(joinEqualIf("userFactoryMappings", "factory.id", filter.getFactoryId()))
                .add(joinEqualIf("userFactoryMappings", "bay.id", filter.getBayId()))
                .build();
    }

    // TOOL
    public static Specification<Tool> forTool(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<Tool>builder()
                .add(like("name", filter.getName()))
                .add(joinEqual("category", "id", filter.getCategoryId()))
                .add(enumEqual("isPerishable", EnumHelper.toPerishable(filter.getPerishable())))
                .add(enumEqual("isExpensive", EnumHelper.toExpensive(filter.getExpensive())))
                .add(between("createdAt", filter.getStartDate(), filter.getEndDate()))
                .build();
    }


    // PRODUCT CATEGORY
    public static Specification<ProductCategory> forProductCategory(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<ProductCategory>builder()
                .add(like("name", filter.getName()))
                .build();
    }

    // TOOL CATEGORY
    public static Specification<ToolCategory> forToolCategory(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<ToolCategory>builder()
                .add(like("name", filter.getName()))
                .build();
    }

    // GLOBAL SEARCH
    public static <T> Specification<T> globalSearch(GenericFilterDto filter, String... fields) {
        if (!hasText(filter.getName())) return (root, query, cb) -> cb.conjunction();
        return GenericSpecificationBuilder.<T>builder()
                .add(globalSearch(filter.getName(), fields))
                .build();
    }

    // === HELPER METHODS ===
    private static <T> GenericSpecificationBuilder.SpecPart<T> like(String field, String value) {
        return (root, cb) -> hasText(value)
                ? cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%")
                : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> equal(String field, Object value) {
        return (root, cb) -> value != null ? cb.equal(root.get(field), value) : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> greaterThanEqual(String field, BigDecimal value) {
        return (root, cb) -> value != null ? cb.greaterThanOrEqualTo(root.get(field), value) : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> lessThanEqual(String field, BigDecimal value) {
        return (root, cb) -> value != null ? cb.lessThanOrEqualTo(root.get(field), value) : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> between(String field, LocalDate start, LocalDate end) {
        return (root, cb) -> {
            if (start == null || end == null) return null;
            ZonedDateTime s = start.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime e = end.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault());
            return cb.between(root.get(field), s, e);
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> joinLike(String join, String field, String value) {
        return (root, cb) -> {
            if (!hasText(value)) return null;
            Join<T, ?> j = root.join(join, JoinType.LEFT);
            return cb.like(cb.lower(j.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> joinEqual(String join, String field, Object value) {
        return (root, cb) -> {
            if (value == null) return null;
            Join<T, ?> j = root.join(join, JoinType.LEFT);
            return cb.equal(j.get(field), value);
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> joinEqualIf(String join, String field, Object value) {
        return (root, cb) -> {
            if (value == null || !hasText(value.toString())) return null;
            Join<T, ?> j = root.join(join, JoinType.INNER);
            return cb.equal(j.get(field), value);
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> globalSearch(String query, String... fields) {
        return (root, cb) -> {
            if (!hasText(query)) return null;
            String like = "%" + query.toLowerCase() + "%";
            List<Predicate> ors = new ArrayList<>();
            for (String f : fields) {
                if (f.contains(".")) {
                    String[] parts = f.split("\\.", 2);
                    Join<T, ?> j = root.join(parts[0], JoinType.LEFT);
                    ors.add(cb.like(cb.lower(j.get(parts[1])), like));
                } else {
                    ors.add(cb.like(cb.lower(root.get(f)), like));
                }
            }
            return cb.or(ors.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> enumEqual(String field, Enum<?> value) {
        return (root, cb) -> value != null ? cb.equal(root.get(field), value) : null;
    }
}
