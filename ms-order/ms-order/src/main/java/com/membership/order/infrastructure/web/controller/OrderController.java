package com.membership.order.infrastructure.web.controller;

import com.membership.order.application.dto.OrderRequestDTO;
import com.membership.order.application.dto.OrderResponseDTO;
import com.membership.order.application.service.OrderService;
import com.membership.order.domain.entity.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "API de gestion des commandes")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping
    @Operation(summary = "Récupérer toutes les commandes")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une commande par ID")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les commandes d'un utilisateur")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les commandes par statut")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }



    @PostMapping
    @Operation(summary = "Créer une commande")
    @ApiResponse(responseCode = "201", description = "Commande créée")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(dto));
    }



    @PutMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une commande")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }


    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une commande")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
