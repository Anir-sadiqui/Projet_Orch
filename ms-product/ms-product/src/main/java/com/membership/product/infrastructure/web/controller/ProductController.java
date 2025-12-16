package com.membership.product.infrastructure.web.controller;

import com.membership.product.application.dto.ProductRequestDTO;
import com.membership.product.application.dto.ProductResponseDTO;
import com.membership.product.application.mapper.ProductMapper;
import com.membership.product.application.service.ProductService;
import com.membership.product.domain.entity.Product;
import com.membership.product.domain.entity.ProductCategory;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.membership.product.application.dto.StockUpdateRequestDTO;

import java.util.List;


@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "API de gestion des produits")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper mapper;

    public ProductController(ProductService productService, ProductMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok(
                productService.findAll()
                        .stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO dto) {

        Product product = mapper.toEntity(dto);
        Product saved = productService.create(product);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(productService.findById(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {

        Product updated = mapper.toEntity(dto);
        return ResponseEntity.ok(
                mapper.toResponse(productService.update(id, updated))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> search(@RequestParam String name) {
        return ResponseEntity.ok(
                productService.searchByName(name)
                        .stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> byCategory(
            @PathVariable ProductCategory category) {

        return ResponseEntity.ok(
                productService.findByCategory(category)
                        .stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductResponseDTO>> available() {
        return ResponseEntity.ok(
                productService.available()
                        .stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequestDTO dto) {

        productService.updateStock(id, dto.getNewStock());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}