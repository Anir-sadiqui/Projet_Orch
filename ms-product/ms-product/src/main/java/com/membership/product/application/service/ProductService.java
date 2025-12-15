package com.membership.product.application.service;
import com.membership.product.infrastructure.exception.ResourceNotFoundException;

import com.membership.product.application.dto.ProductRequestDTO;
import com.membership.product.application.dto.ProductResponseDTO;
import com.membership.product.application.mapper.ProductMapper;
import com.membership.product.domain.entity.Product;
import com.membership.product.domain.entity.ProductCategory;
import com.membership.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = ProductMapper.toEntity(dto);
        Product saved = productRepository.save(product);
        return ProductMapper.toResponse(saved);
    }
    public ProductResponseDTO getProductById(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    return ProductMapper.toResponse(product);
}
public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
    Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

    ProductMapper.updateEntity(existing, dto);
    Product updated = productRepository.save(existing);
    return ProductMapper.toResponse(updated);
}
public List<ProductResponseDTO> searchByName(String name) {
    return productRepository.findByNameContainingIgnoreCase(name)
            .stream()
            .map(ProductMapper::toResponse)
            .collect(Collectors.toList());
}
public List<ProductResponseDTO> getByCategory(String category) {
    ProductCategory productCategory = ProductCategory.fromString(category);
    return productRepository.findByCategory(productCategory)
            .stream()
            .map(ProductMapper::toResponse)
            .collect(Collectors.toList());
}
public List<ProductResponseDTO> getAvailableProducts() {
    return productRepository.findByStockGreaterThanAndActiveTrue(0)
            .stream()
            .map(ProductMapper::toResponse)
            .collect(Collectors.toList());
}
public ProductResponseDTO updateStock(Long id, int quantityChange) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

    int newStock = product.getStock() + quantityChange;
    if (newStock < 0) {
        throw new IllegalArgumentException("Stock cannot be negative");
    }
    product.setStock(newStock);
    product.setUpdatedAt(java.time.LocalDateTime.now());
    Product saved = productRepository.save(product);
    return ProductMapper.toResponse(saved);
}

/**
 * Supprime un produit par son ID.
 * 
 * @param id l'identifiant du produit
 * @throws ResourceNotFoundException si le produit n'existe pas
 */
public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    
    productRepository.delete(product);
}

}
