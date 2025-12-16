package com.membership.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {

    @NotNull
    private Long userId;

    @NotBlank
    private String shippingAddress;

    @NotEmpty
    private List<OrderItemRequestDTO> items;

}
