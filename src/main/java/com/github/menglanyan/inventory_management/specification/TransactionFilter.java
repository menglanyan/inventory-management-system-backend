package com.github.menglanyan.inventory_management.specification;

import com.github.menglanyan.inventory_management.entities.Transaction;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionFilter {

    public  static Specification<Transaction> byFliter(String searchValue) {

        return (root, query, criteriaBuilder) -> {

            // If filter is null or empty, return true for all transactions
            if (searchValue == null || searchValue.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + searchValue.toLowerCase() + "%";

            // Create a list to hold all predicates
            List<Predicate> predicates = new ArrayList<>();

            // Check transactions fields
            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
            );
            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), searchPattern)
            );
            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)), searchPattern)
            );
            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("transactionType").as(String.class)), searchPattern)
            );

            // Safely join and check user fields using LEFT JOIN
            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("user"))) {
                root.join("user", JoinType.LEFT);
            }

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("name")), searchPattern)
            );
            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("email")), searchPattern)
            );
            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("phoneNumber")), searchPattern)
            );

            // Safely join and check supplier fields using LEFT JOIN
            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("supplier"))) {
                root.join("supplier", JoinType.LEFT);
            }

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("supplier", JoinType.LEFT).get("name")), searchPattern)
            );

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("supplier", JoinType.LEFT).get("contactInfo")), searchPattern)
            );

            // Safely join and check product fields using LEFT JOIN
            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("product"))) {
                root.join("product", JoinType.LEFT);
            }

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT).get("name")), searchPattern)
            );

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT).get("sku")), searchPattern)
            );

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT).get("description")), searchPattern)
            );

            // Safely join product category using LEFT JOIN
            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("product")) &&
            root.join("product").getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("category"))) {
                root.join("product", JoinType.LEFT).join("category", JoinType.LEFT);
            }

            predicates.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT)
                                    .join("category", JoinType.LEFT).get("name")), searchPattern)
            );

            // Combine all predicates with OR
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));

        };
    }

    // Filtering transactions by month and year
    public static Specification<Transaction> byMonthAndYear(int month, int year) {

        return (root, query, criteriaBuilder) -> {
            Expression<Integer> monthExpression = criteriaBuilder.function("month", Integer.class, root.get("createdAt"));
            Expression<Integer> yearExpression = criteriaBuilder.function("year", Integer.class, root.get("createdAt"));

            Predicate monthPredicate = criteriaBuilder.equal(monthExpression, month);
            Predicate yearPredicate = criteriaBuilder.equal(yearExpression, year);

            return criteriaBuilder.and(monthPredicate, yearPredicate);

        };
    }
}
