package com.membership.product.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Size(min = 3, max = 100)
    @NotBlank
    private String name;

    @Column(nullable = false, length = 500)
    @Size(min = 10, max = 500)
    @NotBlank
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    @NotNull
    private BigDecimal price;

    @Column(nullable = false)
    @Min(0)
    @NotNull
    private Integer stock;

    @Column(nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
