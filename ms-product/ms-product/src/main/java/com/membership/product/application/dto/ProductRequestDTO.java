package com.membership.product.application.dto;

import com.membership.product.domain.entity.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Data;


import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Size(min = 10, max = 500)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stock;

    @NotNull
    private ProductCategory category;

    private String imageUrl;


}
