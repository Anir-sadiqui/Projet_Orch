#!/usr/bin/env bash

set -e

BASE_DIR="ms-order/ms-order"

echo "==> Création de la structure ms-order..."

# Dossiers Maven de base
mkdir -p "$BASE_DIR/src/main/java/com/membership/order"
mkdir -p "$BASE_DIR/src/main/resources"
mkdir -p "$BASE_DIR/src/test/java/com/membership/order"

# Couches DDD
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/domain/entity"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/domain/repository"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/application/dto"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/application/mapper"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/application/service"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/web/controller"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/exception"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/health"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/client"

echo "==> Création des fichiers Java..."

# Classe principale
cat > "$BASE_DIR/src/main/java/com/membership/order/OrderApplication.java" << 'EOF'
package com.membership.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
EOF

# Domain
cat > "$BASE_DIR/src/main/java/com/membership/order/domain/entity/OrderStatus.java" << 'EOF'
package com.membership.order.domain.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
EOF

cat > "$BASE_DIR/src/main/java/com/membership/order/domain/entity/Order.java" << 'EOF'
package com.membership.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String shippingAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, Long userId, LocalDateTime orderDate, OrderStatus status,
                 BigDecimal totalAmount, String shippingAddress,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // getters / setters ...

}
EOF

cat > "$BASE_DIR/src/main/java/com/membership/order/domain/entity/OrderItem.java" << 'EOF'
package com.membership.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal subtotal;

    public OrderItem() {
    }

    // getters / setters ...

}
EOF

# Repositories
cat > "$BASE_DIR/src/main/java/com/membership/order/domain/repository/OrderRepository.java" << 'EOF'
package com.membership.order.domain.repository;

import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);
}
EOF

cat > "$BASE_DIR/src/main/java/com/membership/order/domain/repository/OrderItemRepository.java" << 'EOF'
package com.membership.order.domain.repository;

import com.membership.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
EOF

# DTO / Mapper / Service / Controller / Exceptions : fichiers vides à remplir ensuite
touch "$BASE_DIR/src/main/java/com/membership/order/application/dto/OrderRequestDTO.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/dto/OrderItemRequestDTO.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/dto/OrderResponseDTO.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/mapper/OrderMapper.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/service/OrderService.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/web/controller/OrderController.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/exception/ResourceNotFoundException.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/exception/GlobalExceptionHandler.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/health/ExternalServicesHealthIndicator.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/client/UserClient.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/client/ProductClient.java"

echo "==> Création de application.yml..."

cat > "$BASE_DIR/src/main/resources/application.yml" << 'EOF'
spring:
  application:
    name: orders
    version: 1.0.0

  datasource:
    url: jdbc:h2:mem:orderdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: ${APP_PORT:8083}

management:
  endpoints:
    web:
      exposure:
#!/usr/bin/env bash

set -e

BASE_DIR="ms-order/ms-order"

echo "==> Création de la structure ms-order..."

# Dossiers Maven de base
mkdir -p "$BASE_DIR/src/main/java/com/membership/order"
mkdir -p "$BASE_DIR/src/main/resources"
mkdir -p "$BASE_DIR/src/test/java/com/membership/order"

# Couches DDD
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/domain/entity"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/domain/repository"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/application/dto"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/application/mapper"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/application/service"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/web/controller"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/exception"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/health"
mkdir -p "$BASE_DIR/src/main/java/com/membership/order/infrastructure/client"

echo "==> Création des fichiers Java..."

# Classe principale
cat > "$BASE_DIR/src/main/java/com/membership/order/OrderApplication.java" << 'EOF'
package com.membership.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
EOF

# Domain
cat > "$BASE_DIR/src/main/java/com/membership/order/domain/entity/OrderStatus.java" << 'EOF'
package com.membership.order.domain.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
EOF

cat > "$BASE_DIR/src/main/java/com/membership/order/domain/entity/Order.java" << 'EOF'
package com.membership.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String shippingAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, Long userId, LocalDateTime orderDate, OrderStatus status,
                 BigDecimal totalAmount, String shippingAddress,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // getters / setters ...

}
EOF

cat > "$BASE_DIR/src/main/java/com/membership/order/domain/entity/OrderItem.java" << 'EOF'
package com.membership.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal subtotal;

    public OrderItem() {
    }

    // getters / setters ...

}
EOF

# Repositories
cat > "$BASE_DIR/src/main/java/com/membership/order/domain/repository/OrderRepository.java" << 'EOF'
package com.membership.order.domain.repository;

import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);
}
EOF

cat > "$BASE_DIR/src/main/java/com/membership/order/domain/repository/OrderItemRepository.java" << 'EOF'
package com.membership.order.domain.repository;

import com.membership.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
EOF

# DTO / Mapper / Service / Controller / Exceptions : fichiers vides à remplir ensuite
touch "$BASE_DIR/src/main/java/com/membership/order/application/dto/OrderRequestDTO.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/dto/OrderItemRequestDTO.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/dto/OrderResponseDTO.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/mapper/OrderMapper.java"
touch "$BASE_DIR/src/main/java/com/membership/order/application/service/OrderService.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/web/controller/OrderController.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/exception/ResourceNotFoundException.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/exception/GlobalExceptionHandler.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/health/ExternalServicesHealthIndicator.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/client/UserClient.java"
touch "$BASE_DIR/src/main/java/com/membership/order/infrastructure/client/ProductClient.java"

echo "==> Création de application.yml..."

cat > "$BASE_DIR/src/main/resources/application.yml" << 'EOF'
spring:
  application:
    name: orders
    version: 1.0.0

  datasource:
    url: jdbc:h2:mem:orderdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: ${APP_PORT:8083}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
EOF

echo "==> Structure ms-order créée."
