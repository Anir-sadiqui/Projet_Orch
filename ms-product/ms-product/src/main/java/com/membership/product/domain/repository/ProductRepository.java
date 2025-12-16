package com.membership.product.domain.repository;

import com.membership.product.domain.entity.Product;
import com.membership.product.domain.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByStockGreaterThanAndActiveTrue(int stock);

    long countByStockLessThan(int stock);
}
