# Document Architecture Technique (DAT)
## Plateforme E-Commerce Microservices

**Date:** December 2024  
**Version:** 1.0  
**Auteurs:** Plateforme de Gestion de Commandes

---

## 1. Vue Générale

### 1.1 Objectif du Système
La plateforme est une architecture microservices destinée à gérer un système e-commerce complet avec :
- **Gestion des utilisateurs (Membership)**
- **Catalogue de produits (Product)**
- **Gestion des commandes (Order)**

### 1.2 Principes Architecturaux
- **Microservices** : Trois services indépendants et autonomes
- **Communication inter-services** : Via HTTP REST (RestTemplate)
- **Domain-Driven Design** : Architecture en couches (presentation, application, domain, infrastructure)
- **Résilience** : Health checks et monitoring intégrés
- **Validation** : Bean Validation + validations métier dans les services

---

## 2. Architecture Globale

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway / Load Balancer               │
└────────────────┬──────────────────────┬──────────────────────┘
                 │                      │
         ┌───────▼────────┐    ┌────────▼──────────┐
         │  ms-membership │    │   ms-product      │
         │    (8080)      │    │     (8082)        │
         ├────────────────┤    ├───────────────────┤
         │ • User CRUD    │    │ • Product CRUD    │
         │ • Auth         │    │ • Stock Mgmt      │
         │ • Profiles     │    │ • Categories      │
         └───────────────┘    └───────────────────┘
                 ▲                      ▲
                 │       HTTP REST      │
         ┌───────┴──────────────────────┴───────┐
         │                                      │
         │      ┌──────────────────────┐       │
         │      │   ms-order (8083)    │       │
         │      ├──────────────────────┤       │
         │      │ • Order CRUD         │       │
         │      │ • Order Management   │       │
         │      │ • User/Product Valid │       │
         │      │ • Stock Deduction    │       │
         │      └──────────────────────┘       │
         │                                      │
         └──────────────────────────────────────┘


```

---

## 3. Architecture en Couches

Chaque microservice suit l'architecture en 4 couches :

### 3.1 Couche Présentation (Web / Infrastructure)
**Responsabilité** : Exposer les endpoints REST

**Composants** :
- `*Controller` : Classes annotées `@RestController`
- `@RequestMapping` : Routage des requêtes
- Validation des entrées avec `@Valid`
- Transformation DTO ↔ Entity

**Exemple** : `ProductController`
```
GET    /api/v1/products
POST   /api/v1/products
GET    /api/v1/products/{id}
PUT    /api/v1/products/{id}
PATCH  /api/v1/products/{id}/stock
DELETE /api/v1/products/{id}

```

### 3.2 Couche Application
Responsabilité : logique métier et orchestration

Composants :

*Service

*DTO

*Mapper

Gestion transactionnelle (@Transactional)

Rôles clés :

Validation métier

Appels inter-services

Calculs (prix, stock, statuts)

Gestion des erreurs

### 3.3 Couche Domaine
Responsabilité : modèle métier pur

Composants :

Entités JPA (@Entity)

Enums métier

Interfaces Repository

Exemple – Product Entity :
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private Integer quantity;
    private Double price;
}

```

### 3.4 Couche Infrastructure
**Responsabilité** : Services techniques (BD, HTTP, monitoring)

**Composants** :
- `*Client` : Clients HTTP pour appels inter-services
- `*Configuration` : Beans Spring, configuration RestTemplate
- `*Exception` : Classes et handlers d'exceptions
- `*Health` : Health Indicators pour monitoring
- `data.sql` : Données de test H2

---

## 4. Flux de Données Inter-Services

### 4.1 Cas d'Usage : Créer une Commande

```
Client
 ↓
OrderController
 ↓
OrderService
 ├─ Vérification utilisateur (ms-membership)
 ├─ Vérification produits (ms-product)
 ├─ Validation du stock
 ├─ Déduction du stock
 ├─ Création commande
 └─ Réponse OrderResponseDTO

```

### 4.2 Dépendances de Communication

| Service | Appelle | Via |
|---------|---------|-----|
| ms-order | ms-membership | `UserClient` (RestTemplate) |
| ms-order | ms-product | `ProductClient` (RestTemplate) |
| ms-product | (aucun) | - |
| ms-membership | (aucun) | - |

---

## 5. Enumerations et Types

### 5.1 ProductCategory Enum

```java
public enum ProductCategory {
    ELECTRONICS("Électronique"),
    BOOKS("Livres"),
    FOOD("Alimentation"),
    OTHER("Autres");
    
    private final String displayName;
    
    public static ProductCategory fromString(String value) {
        try {
            return ProductCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Catégorie invalide: " + value + 
                ". Valeurs acceptées: " + validCategories()
            );
        }
    }
}
```

**Avantage** : Type-safe, validation garantie, évite les erreurs typage

### 5.2 OrderStatus Enum

```java
PENDING      // Nouvellement créée
CONFIRMED    // Confirmée par utilisateur/paiement
SHIPPED      // En cours de livraison
DELIVERED    // Livrée
CANCELLED    // Annulée
```

---

## 6. Gestion des Erreurs

### 6.1 Hiérarchie des Exceptions

```
Exception
├── ResourceNotFoundException extends RuntimeException
│   └── Levée quand : Entity non trouvée en BD
│       HTTP : 404 Not Found
│
└── GlobalExceptionHandler (dans chaque service)
    ├── handleNotFound(ResourceNotFoundException)
    │   └── Répond : 404 + map erreur
    │
    ├── handleIllegalArgument(IllegalArgumentException)
    │   └── Répond : 400 + message erreur métier
    │
    ├── handleValidationException(MethodArgumentNotValidException)
    │   └── Répond : 400 + liste erreurs champs
    │
    └── handleGlobalException(Exception)
        └── Répond : 500 + message générique
```

### 6.2 Réponses d'Erreur

```json
{
  "timestamp": "2024-12-15T10:30:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "Produit avec ID 999 non trouvé",
  "validationErrors": [
    {
      "field": "name",
      "message": "ne doit pas être vide"
    }
  ]
}
```

---

## 7. Validation des Données

### 7.1 Validation au Niveau DTO

```java
@Data
public class ProductRequestDTO {
    @NotBlank(message = "Le nom ne doit pas être vide")
    private String name;
    
    @NotNull(message = "La catégorie est obligatoire")
    private ProductCategory category;
    
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double price;
    
    @NotNull(message = "La quantité est obligatoire")
    @PositiveOrZero(message = "La quantité doit être positive")
    private Integer quantity;
}
```

### 7.2 Validation Métier dans le Service

```java
public OrderResponseDTO createOrder(OrderRequestDTO dto) {
    // 1. Valider que l'utilisateur existe
    if (!userClient.userExists(dto.getUserId())) {
        throw new IllegalArgumentException(
            "Utilisateur avec ID " + dto.getUserId() + " non trouvé"
        );
    }
    
    // 2. Valider que tous les items ont des produits existants
    for (OrderItemDTO item : dto.getItems()) {
        if (!productClient.productExists(item.getProductId())) {
            throw new IllegalArgumentException(
                "Produit avec ID " + item.getProductId() + " non trouvé"
            );
        }
        
        // 3. Valider le stock
        if (!productClient.hasEnoughStock(
            item.getProductId(), 
            item.getQuantity()
        )) {
            throw new IllegalArgumentException(
                "Stock insuffisant pour le produit ID " + item.getProductId()
            );
        }
    }
    
    // 4. Opérations de création
    // ...
}
```

---

## 8. Monitoring et Health Checks

### 8.1 Health Indicators

Chaque service expose `/actuator/health` avec plusieurs indicateurs :

#### ms-product Service

**StockHealthIndicator** (`@Component("stockHealth")`)
```
Status: UP    → Tous les produits ont stock ≥ 5
Status: DOWN  → Au moins un produit a stock < 5

Détails:
{
  "status": "UP|DOWN",
  "components": {
    "stockHealth": {
      "status": "UP|DOWN",
      "details": {
        "lowStockCount": 2,
        "products": ["Product A", "Product B"]
      }
    }
  }
}
```

#### ms-order Service

**ExternalServicesHealthIndicator** (`@Component("externalServices")`)
```
Status: UP    → ms-membership ET ms-product disponibles
Status: DOWN  → Au moins un service indisponible

Détails:
{
  "status": "UP|DOWN",
  "components": {
    "externalServices": {
      "status": "UP|DOWN",
      "details": {
        "userServiceHealthy": true,
        "productServiceHealthy": false,
        "message": "Product service unavailable"
      }
    }
  }
}
```

### 8.2 Endpoints Health

```
# Global health
GET /actuator/health

# Health détaillé avec tous les composants
GET /actuator/health/detailed

# Health spécifique au service
GET /actuator/health/stockHealth       (Product)
GET /actuator/health/externalServices  (Order)
```

---

## 9. Configuration RestTemplate

### 9.1 Configuration (OrderConfiguration)

```java
@Configuration
public class OrderConfiguration {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))    // 5s timeout
            .setReadTimeout(Duration.ofSeconds(5))       // 5s timeout
            .build();
    }
}
```

**Timeouts** :
- **Connect Timeout** : 5 secondes (établir connexion)
- **Read Timeout** : 5 secondes (attendre réponse)

### 9.2 Utilisation dans Clients

```java
@Component
public class ProductClient {
    
    private static final String PRODUCT_SERVICE_URL = 
        "http://localhost:8082/api/v1/products";
    
    private final RestTemplate restTemplate;
    
    public boolean hasEnoughStock(Long productId, Integer requiredQuantity) {
        try {
            String url = PRODUCT_SERVICE_URL + "/" + productId + 
                        "?requiredQuantity=" + requiredQuantity;
            ResponseEntity<Boolean> response = 
                restTemplate.getForEntity(url, Boolean.class);
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody();
        } catch (RestClientException e) {
            logger.error("Erreur vérification stock", e);
            throw new RuntimeException(
                "Impossible de vérifier le stock: " + e.getMessage()
            );
        }
    }
}
```

---

## 10. Gestion du Stock

### 10.1 Flux Création de Commande

```
1. Vérifier stock pour chaque item
   └─→ productClient.hasEnoughStock(productId, qty)

2. SI stock insuffisant → FAIL + levée exception

3. SI stock OK :
   a. Créer Order avec status PENDING
   b. Pour chaque item :
      └─→ productClient.updateStock(productId, -qty)
      
4. SI mise à jour stock échoue :
   → Rollback transaction (aucun changement persisté)
```

### 10.2 Flux Annulation de Commande

```
1. Vérifier status order
   ├─ DELIVERED → Impossible d'annuler (exception)
   ├─ CANCELLED → Impossible d'annuler (exception)
   └─ Autres    → OK

2. Pour chaque item :
   └─→ productClient.updateStock(productId, +qty)
   
3. Mettre à jour status à CANCELLED

4. Sauvegarder changes
```

---

## 11. DTOs et Mappings

### 11.1 Structure DTO Product

```
ProductRequestDTO (entrée)
├─ name: String
├─ category: ProductCategory
├─ price: Double
└─ quantity: Integer

    ↓ Mapper.toEntity(dto)
    
Product (entité JPA)
├─ id: Long
├─ name: String
├─ category: ProductCategory
├─ price: Double
├─ quantity: Integer
└─ createdAt: LocalDateTime

    ↓ Mapper.toResponseDTO(entity)
    
ProductResponseDTO (sortie)
├─ id: Long
├─ name: String
├─ category: String (category.name())
├─ price: Double
└─ quantity: Integer
```

### 11.2 Structure DTO Order

```
OrderRequestDTO (entrée)
├─ userId: Long
└─ items: List<OrderItemDTO>
   ├─ productId: Long
   └─ quantity: Integer

    ↓ Mapper.toEntity(dto)
    
Order (entité JPA)
├─ id: Long
├─ userId: Long
├─ status: OrderStatus
├─ totalPrice: Double
├─ items: List<OrderItem>
└─ createdAt: LocalDateTime

    ↓ Mapper.toResponseDTO(entity)
    
OrderResponseDTO (sortie)
├─ id: Long
├─ userId: Long
├─ status: String (enum.name())
├─ totalPrice: Double
├─ items: List<OrderItemDTO>
└─ createdAt: LocalDateTime
```

---

## 12. Données de Test (data.sql)

### 12.1 ms-product/data.sql

```sql
-- Initialisation produits
INSERT INTO products (name, category, price, quantity) VALUES
('Laptop Dell', 'ELECTRONICS', 999.99, 5),
('Clean Code', 'BOOKS', 45.99, 10),
('Coffee', 'FOOD', 12.99, 0),  -- Stock faible
('Keyboard', 'ELECTRONICS', 79.99, 2);

-- Données de test pour stock < 5
-- Utilisé par StockHealthIndicator
```

### 12.2 ms-membership/data.sql

```sql
-- Initialisation utilisateurs
INSERT INTO users (name, email) VALUES
('Alice Dupont', 'alice@example.com'),
('Bob Martin', 'bob@example.com'),
('Charlie Brown', 'charlie@example.com');
```

### 12.3 ms-order/data.sql

```sql
-- Initialisation commandes pour tests
INSERT INTO orders (user_id, status, total_price) VALUES
(1, 'PENDING', 1299.99),
(2, 'CONFIRMED', 89.98);

-- OrderItems seront créés via relation OneToMany
```

---

## 13. Déploiement et Configuration

### 13.1 Variables d'Environnement

```properties
# application.yml pour chaque service
spring:
  application:
    name: ms-product
  jpa:
    hibernate:
      ddl-auto: create-drop  # Pour tests
    show-sql: false
  h2:
    console:
      enabled: true

server:
  port: 8082
  servlet:
    context-path: /
```

### 13.2 Ports des Services

| Service | Port |
|---------|------|
| ms-membership | 8080 |
| ms-product | 8082 |
| ms-order | 8083 |

### 13.3 URLs de Test

```bash
# Product Service
curl http://localhost:8082/api/v1/products
curl http://localhost:8082/api/v1/products/1
curl http://localhost:8082/actuator/health

# Order Service
curl http://localhost:8083/api/v1/orders
curl http://localhost:8083/api/v1/orders/1
curl http://localhost:8083/actuator/health

# Membership Service
curl http://localhost:8080/api/v1/users
curl http://localhost:8080/actuator/health
```

---

## 14. Swagger/OpenAPI

### 14.1 Configuration

Spring Boot 3.5.7 inclut Swagger automatiquement avec :
- `io.swagger.v3:springdoc-openapi-starter-webmvc-ui`
- Génération auto des schemas OpenAPI 3.0

### 14.2 Annotations Utilisées

```java
@RestController
@Tag(name = "Products", description = "API de gestion des produits")
public class ProductController {
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un produit",
        description = "Retourne un produit spécifique par ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit trouvé"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponseDTO> getProductById(
        @PathVariable 
        @Parameter(description = "ID du produit") 
        Long id
    ) { }
}
```

### 14.3 Accès à Swagger UI

```
http://localhost:8082/swagger-ui.html    (ms-product)
http://localhost:8083/swagger-ui.html    (ms-order)
http://localhost:8080/swagger-ui.html    (ms-membership)
```

---

## 15. Points de Vigilance et Considérations de Production

### 15.1 Limitations Actuelles

| Aspect | Limitation | Recommendation |
|--------|-----------|-----------------|
| Base de données | H2 en-mémoire | Utiliser PostgreSQL/MySQL en production |
| Cache | Aucun | Ajouter Redis/Memcached pour hot products |
| Transaction distribuée | Non géré | Implémenter Saga pattern pour ordres critiques |
| Authentification | Aucune | Ajouter OAuth2/Spring Security |
| Rate limiting | Absent | Utiliser Spring Cloud gateway |
| Resilience | Basique | Ajouter Hystrix/Resilience4j |

### 15.2 Considérations Futures

1. **Saga Pattern** : Pour transactions distribuées sur Order
2. **Event Sourcing** : Traçabilité complète des changements
3. **CQRS** : Séparation lecture/écriture pour les rapports
4. **Circuit Breaker** : Résilience inter-service avec Resilience4j
5. **Service Mesh** : Istio/Linkerd pour observabilité avancée
6. **Authentification** : Spring Security + OAuth2
7. **Cache distribué** : Redis pour produits populaires
8. **API Gateway** : Spring Cloud Gateway ou Kong

---

## 16. Diagramme de Séquence : Créer une Commande

```
Client                    Order             Order         Product        User
 |                       Controller        Service        Client         Client
 |                           |              |              |              |
 |----CreateOrder-------->|   |              |              |              |
 |  (OrderRequestDTO)      |   |              |              |              |
 |                        |   |--check user-->|              |              |
 |                        |   |<--exists------|              |              |
 |                        |   |--validate product-->|        |              |
 |                        |   |<--exists------|------->|     |
 |                        |   |<--exists------|<------|     |
 |                        |   |--hasEnoughStock-->|         |              |
 |                        |   |<--true/false------|         |              |
 |                        |   |--deductStock->|            |              |
 |                        |   |<--ok/error-----|            |              |
 |                        |   |              |              |              |
 |                        |   |--create Order in DB-|       |              |
 |                        |   |<--Order created------|       |              |
 |                        |   |              |              |              |
 |<--OrderResponseDTO-----|   |              |              |              |
 |  (201 Created)         |   |              |              |              |
```

---

## 17. Glossaire

| Terme | Définition |
|-------|-----------|
| **DTO** | Data Transfer Object - Objet pour transférer données via API |
| **Entity** | Classe annotée `@Entity` mappée à une table BD |
| **Repository** | Interface d'accès aux données (CRUD) |
| **Service** | Classe contenant logique métier transactionnelle |
| **Controller** | Classe exposant endpoints REST |
| **Mapper** | Classe pour transformer Entity ↔ DTO |
| **Health Indicator** | Composant rapportant la santé d'un système |
| **RestTemplate** | Client HTTP Spring pour appels synchrones |
| **Exception Handler** | Gestionnaire centralisé d'exceptions |
| **Enum** | Type énuméré avec valeurs fixes |

---

## 18. Conclusion

Cette architecture microservices fournit :

✅ **Modularité** : Services indépendants, déployables séparément  
✅ **Maintenabilité** : Couches claires, responsabilités séparées  
✅ **Scalabilité** : Chaque service peut être scalé indépendamment  
✅ **Monitoring** : Health checks et observabilité intégrée  
✅ **Validation** : Multi-niveaux (DTO + métier)  
✅ **Erreurs** : Gestion centralisée et cohérente  
✅ **Documentation** : Swagger/OpenAPI automatique  

Elle constitue une base solide pour une plateforme e-commerce, avec des possibilités d'extension vers des patterns avancés (Saga, Event Sourcing, Circuit Breaker, etc.).

---

**Document de référence pour la soutenance TP1**  
*Architecture conçue pour respecter les critères académiques*
