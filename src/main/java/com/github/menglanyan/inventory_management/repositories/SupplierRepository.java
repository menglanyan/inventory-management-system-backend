package com.github.menglanyan.inventory_management.repositories;

import com.github.menglanyan.inventory_management.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
