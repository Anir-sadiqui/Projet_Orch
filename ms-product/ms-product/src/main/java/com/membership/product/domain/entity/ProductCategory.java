package com.membership.product.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ProductCategory {
    ELECTRONICS("Électronique"),
    BOOKS("Livres"),
    FOOD("Nourriture"),
    OTHER("Autres");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

}
