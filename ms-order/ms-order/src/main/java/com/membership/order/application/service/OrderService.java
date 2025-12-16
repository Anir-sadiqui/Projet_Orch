package com.membership.order.application.service;

import com.membership.order.application.dto.OrderItemRequestDTO;
import com.membership.order.application.dto.OrderRequestDTO;
import com.membership.order.application.dto.OrderResponseDTO;
import com.membership.order.application.mapper.OrderMapper;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderItem;
import com.membership.order.domain.entity.OrderStatus;
import com.membership.order.domain.repository.OrderRepository;
import com.membership.order.infrastructure.client.ProductClient;
import com.membership.order.infrastructure.client.UserClient;
import com.membership.order.infrastructure.client.dto.ProductDTO;
import com.membership.order.infrastructure.exception.ResourceNotFoundException;
import com.membership.order.infrastructure.metrics.OrderMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderMetrics orderMetrics;

    public OrderService(OrderRepository orderRepository,
                        UserClient userClient,
                        ProductClient productClient,
                        OrderMetrics orderMetrics) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.productClient = productClient;
        this.orderMetrics = orderMetrics;
    }

    public OrderResponseDTO createOrder(OrderRequestDTO dto) {


        if (!userClient.userExists(dto.getUserId())) {
            throw new ResourceNotFoundException("User", "id", dto.getUserId());
        }


        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Une commande doit contenir au moins un article"
            );
        }

        Order order = OrderMapper.toEntity(dto);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;


        for (OrderItemRequestDTO itemDto : dto.getItems()) {

            ProductDTO product = productClient.getProduct(itemDto.getProductId());

            if (product.getStock() < itemDto.getQuantity()) {
                throw new IllegalStateException(
                        "Stock insuffisant pour le produit " + product.getId()
                );
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(product.getPrice());

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            item.setSubtotal(subtotal);
            item.setOrder(order);

            order.getItems().add(item);
            totalAmount = totalAmount.add(subtotal);


            productClient.updateStock(
                    product.getId(),
                    -itemDto.getQuantity()
            );

        }

        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);


        orderMetrics.incrementStatus(OrderStatus.PENDING);
        orderMetrics.addRevenue(totalAmount);

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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order", "id", id));
        return OrderMapper.toResponse(order);
    }

    public List<OrderResponseDTO> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Une commande " + order.getStatus() + " ne peut pas être modifiée"
            );
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        orderMetrics.incrementStatus(newStatus);

        return OrderMapper.toResponse(order);
    }

    public OrderResponseDTO cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Impossible d'annuler une commande livrée"
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Commande déjà annulée"
            );
        }


        order.getItems().forEach(item -> {
            productClient.updateStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        });


        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        orderMetrics.incrementStatus(OrderStatus.CANCELLED);

        return OrderMapper.toResponse(order);
    }
}
