package com.membership.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class OrderItemRequestDTO {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;

}
