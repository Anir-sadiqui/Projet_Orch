package com.membership.product.infrastructure.web.controller;

import com.membership.product.application.dto.ProductRequestDTO;
import com.membership.product.application.dto.ProductResponseDTO;
import com.membership.product.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.membership.product.application.dto.StockUpdateRequestDTO;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des produits.
 * Expose tous les endpoints CRUD et les opérations de recherche.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "API de gestion des produits")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Récupère la liste de tous les produits.
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les produits", description = "Retourne la liste complète des produits")
    @ApiResponse(responseCode = "200", description = "Liste des produits récupérée avec succès")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Crée un nouveau produit.
     */
    @PostMapping
    @Operation(summary = "Créer un nouveau produit", description = "Crée et retourne un nouveau produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Erreur de validation des données")
    })
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère un produit par son ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID", description = "Retourne un produit spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit récupéré avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable @Parameter(description = "ID du produit") Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Met à jour un produit existant.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un produit", description = "Met à jour un produit existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé"),
        @ApiResponse(responseCode = "400", description = "Erreur de validation des données")
    })
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable @Parameter(description = "ID du produit") Long id,
            @Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Recherche les produits par nom.
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des produits par nom", description = "Retourne les produits correspondant au nom")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés")
    public ResponseEntity<List<ProductResponseDTO>> searchProductsByName(
            @RequestParam @Parameter(description = "Nom du produit à rechercher") String name) {
        List<ProductResponseDTO> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupère les produits par catégorie.
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Récupérer les produits par catégorie", description = "Retourne les produits d'une catégorie")
    @ApiResponse(responseCode = "200", description = "Produits de la catégorie récupérés")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(
            @PathVariable @Parameter(description = "Catégorie (ELECTRONICS, BOOKS, FOOD, OTHER)") String category) {
        List<ProductResponseDTO> products = productService.getByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * Récupère les produits disponibles (stock > 0).
     */
    @GetMapping("/available")
    @Operation(summary = "Récupérer les produits disponibles", description = "Retourne les produits en stock")
    @ApiResponse(responseCode = "200", description = "Produits disponibles récupérés")
    public ResponseEntity<List<ProductResponseDTO>> getAvailableProducts() {
        List<ProductResponseDTO> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Met à jour le stock d'un produit.
     */
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Mettre à jour le stock", description = "Augmente ou diminue le stock d'un produit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock mis à jour"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponseDTO> updateStock(
            @PathVariable @Parameter(description = "ID du produit") Long id,
            @Valid @RequestBody StockUpdateRequestDTO dto) {
        ProductResponseDTO updated = productService.updateStock(id, dto.getQuantityChange());
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime un produit.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit", description = "Supprime un produit existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<Void> deleteProduct(
            @PathVariable @Parameter(description = "ID du produit à supprimer") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
