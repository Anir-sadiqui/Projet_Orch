package com.membership.product.domain.entity;

/**
 * Énumération des catégories de produits disponibles.
 * Utilisée pour valider et classifier les produits.
 */
public enum ProductCategory {
    ELECTRONICS("Électronique"),
    BOOKS("Livres"),
    FOOD("Nourriture"),
    OTHER("Autres");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retourne l'énumération correspondant au nom fourni.
     * Lance IllegalArgumentException si le nom n'existe pas.
     * 
     * @param categoryName le nom de la catégorie
     * @return l'énumération ProductCategory correspondante
     */
    public static ProductCategory fromString(String categoryName) {
        try {
            return ProductCategory.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Catégorie invalide: " + categoryName + 
                ". Catégories valides: ELECTRONICS, BOOKS, FOOD, OTHER"
            );
        }
    }
}
