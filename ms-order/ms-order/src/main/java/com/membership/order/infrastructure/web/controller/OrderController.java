package com.membership.order.infrastructure.web.controller;

import com.membership.order.application.dto.OrderRequestDTO;
import com.membership.order.application.dto.OrderResponseDTO;
import com.membership.order.application.service.OrderService;
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

import java.util.List;

/**
 * Contrôleur REST pour la gestion des commandes.
 * Expose tous les endpoints CRUD et les opérations sur les commandes.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "API de gestion des commandes")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Récupère la liste de toutes les commandes.
     */
    @GetMapping
    @Operation(summary = "Récupérer toutes les commandes", description = "Retourne la liste complète des commandes")
    @ApiResponse(responseCode = "200", description = "Liste des commandes récupérée avec succès")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Crée une nouvelle commande.
     */
    @PostMapping
    @Operation(summary = "Créer une nouvelle commande", description = "Crée une commande avec validation d'utilisateur et de stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Commande créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Erreur de validation (utilisateur non trouvé, produit non trouvé, stock insuffisant)"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la création de la commande")
    })
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO created = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupère une commande par son ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une commande par ID", description = "Retourne une commande spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable @Parameter(description = "ID de la commande") Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Récupère les commandes d'un utilisateur.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les commandes d'un utilisateur", description = "Retourne toutes les commandes d'un utilisateur spécifique")
    @ApiResponse(responseCode = "200", description = "Commandes de l'utilisateur récupérées")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(
            @PathVariable @Parameter(description = "ID de l'utilisateur") Long userId) {
        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Récupère les commandes par statut.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les commandes par statut", description = "Retourne les commandes avec un statut spécifique")
    @ApiResponse(responseCode = "200", description = "Commandes récupérées")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable @Parameter(description = "Statut (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)") String status) {
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Met à jour le statut d'une commande.
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une commande", description = "Mets à jour le statut d'une commande existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
        @ApiResponse(responseCode = "400", description = "Tentative de modifier une commande DELIVERED ou CANCELLED"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable @Parameter(description = "ID de la commande") Long id,
            @RequestParam @Parameter(description = "Nouveau statut") String status) {
        OrderResponseDTO updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime une commande.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commande", description = "Supprime une commande existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Commande supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<Void> deleteOrder(
            @PathVariable @Parameter(description = "ID de la commande à supprimer") Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Annule une commande et restaure le stock.
     */
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une commande", description = "Annule une commande et restaure le stock des produits")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande annulée avec succès"),
        @ApiResponse(responseCode = "400", description = "La commande est déjà DELIVERED ou CANCELLED"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable @Parameter(description = "ID de la commande à annuler") Long id) {
        OrderResponseDTO cancelled = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelled);
    }
}
