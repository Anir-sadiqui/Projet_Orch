package com.membership.order.application.mapper;

import com.membership.order.application.dto.OrderItemResponseDTO;
import com.membership.order.application.dto.OrderRequestDTO;
import com.membership.order.application.dto.OrderResponseDTO;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(OrderRequestDTO dto) {
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setShippingAddress(dto.getShippingAddress());
        order.setItems(new ArrayList<>());

        return order;
    }

    public static OrderResponseDTO toResponse(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponseDTO> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList());

        dto.setItems(items);

        return dto;
    }

    private static OrderItemResponseDTO toItemResponse(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();

        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}
