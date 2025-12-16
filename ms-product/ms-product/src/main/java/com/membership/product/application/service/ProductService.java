package com.membership.product.application.service;

import com.membership.product.domain.entity.Product;
import com.membership.product.domain.entity.ProductCategory;
import com.membership.product.domain.repository.ProductRepository;
import com.membership.product.infrastructure.exception.ResourceNotFoundException;
import com.membership.product.infrastructure.metrics.ProductMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final ProductMetrics productMetrics;

    public ProductService(ProductRepository repository,
                          ProductMetrics productMetrics) {
        this.repository = repository;
        this.productMetrics = productMetrics;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product create(Product product) {
        Product saved = repository.save(product);

        productMetrics.incrementProductCreated(saved.getCategory());

        return saved;
    }

    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "id", id));
    }

    public Product update(Long id, Product updated) {
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setCategory(updated.getCategory());
        existing.setImageUrl(updated.getImageUrl());
        return existing;
    }

    public List<Product> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> findByCategory(ProductCategory category) {
        return repository.findByCategory(category);
    }

    public List<Product> available() {
        return repository.findByStockGreaterThanAndActiveTrue(0);
    }

    public void updateStock(Long id, Integer newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        Product product = findById(id);
        product.setStock(newStock);
    }

    public void delete(Long id) {
        Product product = findById(id);
        throw new IllegalStateException(
                "Product cannot be deleted if it is part of an order");
    }
}
