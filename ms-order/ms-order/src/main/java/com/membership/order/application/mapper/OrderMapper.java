package com.membership.order.application.mapper;

import com.membership.order.application.dto.*;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderItem;
import com.membership.order.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(OrderRequestDTO dto) {
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setShippingAddress(dto.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemDto.getProductId());
            item.setProductName(itemDto.getProductName());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            BigDecimal subtotal = itemDto.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            item.setSubtotal(subtotal);

            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        return order;
    }

    public static OrderResponseDTO toResponse(Order order) {
        List<OrderItemResponseDTO> items = order.getItems()
                .stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private static OrderItemResponseDTO toItemResponse(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
