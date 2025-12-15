package com.membership.order.application.service;

import com.membership.order.application.dto.OrderItemRequestDTO;
import com.membership.order.application.dto.OrderRequestDTO;
import com.membership.order.application.dto.OrderResponseDTO;
import com.membership.order.application.mapper.OrderMapper;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderStatus;
import com.membership.order.domain.repository.OrderRepository;
import com.membership.order.infrastructure.client.ProductClient;
import com.membership.order.infrastructure.client.UserClient;
import com.membership.order.infrastructure.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des commandes.
 * Gère la création, modification et consultation des commandes.
 * Responsable de la validation auprès des services User et Product.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, 
                       UserClient userClient, 
                       ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.productClient = productClient;
    }

    /**
     * Crée une nouvelle commande.
     * Valide que l'utilisateur existe, les produits sont disponibles,
     * et le stock est suffisant. Déduit le stock des produits.
     * 
     * @param dto les données de la commande
     * @return la commande créée
     * @throws RuntimeException si validation échoue
     */
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        logger.info("Création de commande pour l'utilisateur {}", dto.getUserId());
        
        // Validation 1: Utilisateur existe
        if (!userClient.userExists(dto.getUserId())) {
            logger.error("Utilisateur {} n'existe pas", dto.getUserId());
            throw new RuntimeException("Utilisateur avec ID " + dto.getUserId() + " n'existe pas");
        }
        
        // Validation 2: Items non vides
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            logger.error("Commande sans articles");
            throw new IllegalArgumentException("Une commande doit contenir au moins un article");
        }
        
        // Validation 3: Chaque produit existe et a du stock
        for (OrderItemRequestDTO item : dto.getItems()) {
            if (!productClient.productExists(item.getProductId())) {
                logger.error("Produit {} n'existe pas", item.getProductId());
                throw new RuntimeException("Produit avec ID " + item.getProductId() + " n'existe pas");
            }
            
            if (!productClient.hasEnoughStock(item.getProductId(), item.getQuantity())) {
                logger.error("Stock insuffisant pour le produit {}", item.getProductId());
                throw new RuntimeException("Stock insuffisant pour le produit ID " + item.getProductId());
            }
        }
        
        // Créer la commande
        Order order = OrderMapper.toEntity(dto);
        Order saved = orderRepository.save(order);
        
        // Déduire le stock pour chaque produit
        for (OrderItemRequestDTO item : dto.getItems()) {
            try {
                productClient.updateStock(item.getProductId(), -item.getQuantity());
                logger.info("Stock déduit pour produit {}: -{}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                logger.error("Erreur lors de la déduction du stock pour le produit {}", item.getProductId(), e);
                // La commande est créée mais le stock n'a pas pu être déduit
                // En production, il faudrait un système de saga/transaction distribuée
            }
        }
        
        logger.info("Commande {} créée avec succès", saved.getId());
        return OrderMapper.toResponse(saved);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderMapper.toResponse(order);
    }

    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO updateOrderStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Vérification: une commande DELIVERED ou CANCELLED ne peut pas être modifiée
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            logger.warn("Tentative de modification d'une commande {} avec statut {}", id, order.getStatus());
            throw new IllegalArgumentException(
                "Une commande avec le statut " + order.getStatus() + " ne peut pas être modifiée"
            );
        }

        OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
        logger.info("Mise à jour statut commande {}: {} -> {}", id, order.getStatus(), status);
        
        order.setStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());

        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    /**
     * Annule une commande.
     * Restitue le stock des produits si la commande peut être annulée.
     * 
     * @param id l'identifiant de la commande
     * @throws RuntimeException si la commande ne peut pas être annulée
     */
    public OrderResponseDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Vérification: ne peut pas annuler une commande DELIVERED ou CANCELLED
        if (order.getStatus() == OrderStatus.DELIVERED) {
            logger.warn("Tentative d'annulation d'une commande livrée {}", id);
            throw new IllegalArgumentException("Impossible d'annuler une commande déjà livrée");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            logger.warn("Tentative d'annulation d'une commande déjà annulée {}", id);
            throw new IllegalArgumentException("Cette commande a déjà été annulée");
        }

        logger.info("Annulation de la commande {}", id);
        
        // Restituer le stock pour chaque produit
        order.getItems().forEach(item -> {
            try {
                productClient.updateStock(item.getProductId(), item.getQuantity());
                logger.info("Stock restitué pour produit {}: +{}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                logger.error("Erreur lors de la restitution du stock pour le produit {}", item.getProductId(), e);
            }
        });

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        
        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    /**
     * Supprime une commande.
     * 
     * @param id l'identifiant de la commande
     */
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        logger.info("Suppression de la commande {}", id);
        orderRepository.delete(order);
    }
}
