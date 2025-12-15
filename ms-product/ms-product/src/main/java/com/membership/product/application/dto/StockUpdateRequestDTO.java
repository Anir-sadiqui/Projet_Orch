package com.membership.product.application.dto;

import jakarta.validation.constraints.NotNull;

public class StockUpdateRequestDTO {

    @NotNull
    private Integer quantityChange; // positif ou négatif

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }
}
