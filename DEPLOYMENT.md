# Guide de Déploiement - Plateforme E-Commerce Microservices

**Version:** 1.0  
**Date:** December 2024  
**Environnement:** Local Development, Testing

---

## Table des Matières

1. [Prérequis](#prérequis)
2. [Installation Locale](#installation-locale)
3. [Configuration des Services](#configuration-des-services)
4. [Démarrage des Services](#démarrage-des-services)
5. [Vérification du Déploiement](#vérification-du-déploiement)
6. [Tests et Validation](#tests-et-validation)
7. [Dépannage](#dépannage)
8. [Endpoints Disponibles](#endpoints-disponibles)
9. [Monitoring](#monitoring)
10. [Arrêt des Services](#arrêt-des-services)

---

## Prérequis

### Logiciels Requis

- **Java JDK** : Version 21 ou supérieure (utilisé : JDK 25)
- **Maven** : Version 3.9.0 ou supérieure (utilisé : 3.13.0)
- **Git** : Pour cloner le repository
- **Terminal/PowerShell** : Pour exécuter les commandes

### Vérification des Prérequis

```powershell
# Vérifier Java
java -version

# Vérifier Maven
mvn --version

# Résultat attendu
# Apache Maven 3.13.0 (...)
# Java version: 25 (ou 21+)
```

**Sortie attendue :**
```
java version "25" ...
Apache Maven 3.13.0
```

### Espace Disque

- **Minimum** : 1 GB (dépendances Maven + JAR)
- **Recommandé** : 2 GB

### Ports Réseau Disponibles

Les services utilisent les ports suivants, assurez-vous qu'ils sont libres :

| Service | Port | À Vérifier |
|---------|------|-----------|
| ms-membership | 8080 | `netstat -ano \| grep :8080` (Windows) |
| ms-product | 8082 | `netstat -ano \| grep :8082` (Windows) |
| ms-order | 8083 | `netstat -ano \| grep :8083` (Windows) |

---

## Installation Locale

### 1. Cloner le Repository

```bash
git clone <repository-url>
cd plateforme-ecommerce-microservices
```

### 2. Structure du Projet

```
plateforme-ecommerce-microservices/
├── ms-membership/
│   └── ms-membership/          # Service utilisateurs
│       ├── pom.xml
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/membership/users/
│       │   │   └── resources/
│       │   └── test/
│       └── target/
├── ms-product/
│   └── ms-product/             # Service produits
│       ├── pom.xml
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/membership/product/
│       │   │   └── resources/
│       │   └── test/
│       └── target/
├── ms-order/
│   └── ms-order/               # Service commandes
│       ├── pom.xml
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/membership/order/
│       │   │   └── resources/
│       │   └── test/
│       └── target/
├── architecture/
│   └── DAT.md                  # Document technique
├── README.md
└── DEPLOYMENT.md               # Ce fichier
```

### 3. Télécharger les Dépendances Maven

```powershell
cd ms-membership/ms-membership
mvn clean install -DskipTests

cd ../../ms-product/ms-product
mvn clean install -DskipTests

cd ../../ms-order/ms-order
mvn clean install -DskipTests
```

**Durée estimée :** 5-10 minutes (première exécution)  
**Connexion Internet** : Requise

---

## Configuration des Services

### Configuration Spring Boot

Chaque service possède un fichier `application.yml` :

**ms-membership/src/main/resources/application.yml**
```yaml
spring:
  application:
    name: ms-membership
  datasource:
    url: jdbc:h2:mem:membershipdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false

server:
  port: 8080
  servlet:
    context-path: /

logging:
  level:
    root: INFO
    com.membership: DEBUG
```

**ms-product/src/main/resources/application.yml**
```yaml
spring:
  application:
    name: ms-product
  datasource:
    url: jdbc:h2:mem:productdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false

server:
  port: 8082
  servlet:
    context-path: /

logging:
  level:
    root: INFO
    com.membership: DEBUG
```

**ms-order/src/main/resources/application.yml**
```yaml
spring:
  application:
    name: ms-order
  datasource:
    url: jdbc:h2:mem:orderdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false

server:
  port: 8083
  servlet:
    context-path: /

logging:
  level:
    root: INFO
    com.membership: DEBUG
```

### Configuration des Clients HTTP

**OrderConfiguration.java** (ms-order)
```java
@Configuration
public class OrderConfiguration {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    }
}
```

**URLs Internes** (en dur pour local)
```
UserClient    → http://localhost:8080/api/v1/users
ProductClient → http://localhost:8082/api/v1/products
```

---

## Démarrage des Services

### Méthode 1 : Démarrage Manuel (Recommandé pour Développement)

#### Terminal 1 - ms-membership

```powershell
cd ms-membership/ms-membership
mvn spring-boot:run
```

Vous devriez voir :
```
Tomcat started on port(s): 8080 (http) with context path ''
Started MembershipApplication in 3.5 seconds
```

#### Terminal 2 - ms-product

```powershell
cd ms-product/ms-product
mvn spring-boot:run
```

Vous devriez voir :
```
Tomcat started on port(s): 8082 (http) with context path ''
Started ProductApplication in 3.8 seconds
```

#### Terminal 3 - ms-order

```powershell
cd ms-order/ms-order
mvn spring-boot:run
```

Vous devriez voir :
```
Tomcat started on port(s): 8083 (http) with context path ''
Started OrderApplication in 4.2 seconds
```

**⏱️ Durée totale de démarrage** : ~15-20 secondes

### Méthode 2 : Démarrage avec JAR Compilé

```powershell
# Compiler d'abord
cd ms-membership/ms-membership
mvn clean package -DskipTests

# Puis démarrer
java -jar target/ms-membership-1.0.0-SNAPSHOT.jar
```

---

## Vérification du Déploiement

### 1. Vérifier que les Services Répondent

```powershell
# Test ms-membership
curl http://localhost:8080/actuator/health
# Réponse attendue: {"status":"UP"}

# Test ms-product
curl http://localhost:8082/actuator/health
# Réponse attendue: {"status":"UP"}

# Test ms-order
curl http://localhost:8083/actuator/health
# Réponse attendue: {"status":"UP"}
```

**Résultat positif :**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    ...
  }
}
```

### 2. Vérifier la Disponibilité des Endpoints

```powershell
# Récupérer tous les produits
curl http://localhost:8082/api/v1/products

# Récupérer tous les utilisateurs
curl http://localhost:8080/api/v1/users

# Récupérer toutes les commandes
curl http://localhost:8083/api/v1/orders
```

### 3. Vérifier la Génération de Données de Test

Les fichiers `data.sql` sont exécutés au démarrage :

```powershell
# Devrait retourner au moins 1 produit
curl http://localhost:8082/api/v1/products | jq '.length'

# Devrait retourner au moins 1 utilisateur
curl http://localhost:8080/api/v1/users | jq '.length'
```

---

## Tests et Validation

### Test 1 : Créer un Produit

```powershell
$productData = @{
    name = "Laptop"
    category = "ELECTRONICS"
    price = 999.99
    quantity = 5
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $productData `
  http://localhost:8082/api/v1/products
```

**Réponse attendue :**
```json
{
  "id": 6,
  "name": "Laptop",
  "category": "ELECTRONICS",
  "price": 999.99,
  "quantity": 5
}
```

### Test 2 : Créer une Commande

```powershell
$orderData = @{
    userId = 1
    items = @(
        @{
            productId = 1
            quantity = 2
        }
    )
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $orderData `
  http://localhost:8083/api/v1/orders
```

**Réponse attendue :**
```json
{
  "id": 3,
  "userId": 1,
  "status": "PENDING",
  "totalPrice": 1999.98,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 999.99
    }
  ]
}
```

### Test 3 : Validation des Erreurs

```powershell
# Catégorie invalide
$invalidData = @{
    name = "Invalid"
    category = "INVALID_CATEGORY"
    price = 100
    quantity = 1
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $invalidData `
  http://localhost:8082/api/v1/products

# Réponse attendue : 400 Bad Request
# Message : "Catégorie invalide..."
```

### Test 4 : Vérifier la Communication Inter-Services

```powershell
# Créer une commande avec utilisateur inexistant
$invalidOrder = @{
    userId = 99999
    items = @(
        @{
            productId = 1
            quantity = 1
        }
    )
} | ConvertTo-Json

curl -X POST `
  -H "Content-Type: application/json" `
  -d $invalidOrder `
  http://localhost:8083/api/v1/orders

# Réponse attendue : 400 Bad Request
# Message : "Utilisateur avec ID 99999 non trouvé"
```

---

## Dépannage

### Problème 1 : Port Déjà en Utilisation

**Symptôme :**
```
Address already in use: bind
```

**Solution :**
```powershell
# Identifier le processus utilisant le port
netstat -ano | findstr :8082

# Arrêter le processus (remplacer PID)
taskkill /PID 5432 /F

# Ou changer le port dans application.yml
server.port: 8085
```

### Problème 2 : Maven Build Échoue

**Symptôme :**
```
[ERROR] COMPILATION ERROR
```

**Solution :**
```powershell
# Nettoyer le cache Maven
mvn clean

# Réinstaller les dépendances
mvn install -U -DskipTests

# Vérifier Java version
java -version  # Doit être 21+
```

### Problème 3 : Commande HTTP Échoue

**Symptôme :**
```
Unable to resolve host
```

**Solution :**
```powershell
# Vérifier que les services sont démarrés
curl http://localhost:8082/actuator/health

# Si non disponible, redémarrer le service
mvn spring-boot:run
```

### Problème 4 : H2 Database Non Initialisée

**Symptôme :**
```
Table "USERS" not found
```

**Solution :**
```powershell
# data.sql est chargé au démarrage automatiquement
# Vérifier la présence du fichier :
# src/main/resources/data.sql

# Redémarrer le service pour recharger les données
```

### Problème 5 : Validations Non Respectées

**Symptôme :**
```
Ne reçoit pas d'erreur pour des données invalides
```

**Solution :**
```java
// Vérifier que @Valid est présent sur le paramètre
@PostMapping
public ResponseEntity<ProductResponseDTO> createProduct(
    @Valid @RequestBody ProductRequestDTO dto  // @Valid obligatoire
) { }
```

---

## Endpoints Disponibles

### ms-product (Port 8082)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/products` | Récupérer tous les produits |
| GET | `/api/v1/products/{id}` | Récupérer un produit par ID |
| POST | `/api/v1/products` | Créer un produit |
| PUT | `/api/v1/products/{id}` | Mettre à jour un produit |
| PATCH | `/api/v1/products/{id}/stock` | Mettre à jour le stock |
| DELETE | `/api/v1/products/{id}` | Supprimer un produit |
| GET | `/api/v1/products/search?name=...` | Rechercher par nom |
| GET | `/api/v1/products/category/{cat}` | Récupérer par catégorie |
| GET | `/api/v1/products/available` | Produits en stock |

### ms-order (Port 8083)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/orders` | Récupérer toutes les commandes |
| GET | `/api/v1/orders/{id}` | Récupérer une commande |
| POST | `/api/v1/orders` | Créer une commande |
| PUT | `/api/v1/orders/{id}/status` | Mettre à jour le statut |
| PATCH | `/api/v1/orders/{id}/cancel` | Annuler une commande |
| DELETE | `/api/v1/orders/{id}` | Supprimer une commande |
| GET | `/api/v1/orders/user/{userId}` | Commandes d'un utilisateur |
| GET | `/api/v1/orders/status/{status}` | Commandes par statut |

### ms-membership (Port 8080)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/users` | Récupérer tous les utilisateurs |
| GET | `/api/v1/users/{id}` | Récupérer un utilisateur |
| POST | `/api/v1/users` | Créer un utilisateur |
| PUT | `/api/v1/users/{id}` | Mettre à jour un utilisateur |
| DELETE | `/api/v1/users/{id}` | Supprimer un utilisateur |

### Health & Monitoring (Tous les services)

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Santé globale du service |
| `/actuator/health/detailed` | Santé détaillée |
| `/actuator/health/stockHealth` | Santé du stock (ms-product) |
| `/actuator/health/externalServices` | Santé services externes (ms-order) |

---

## Monitoring

### 1. Accès aux Health Checks

```powershell
# Health de ms-product
curl http://localhost:8082/actuator/health/detailed | jq

# Vérifier le stock
curl http://localhost:8082/actuator/health/stockHealth | jq

# Exemple de réponse :
#{
#  "status": "UP",
#  "components": {
#    "stockHealth": {
#      "status": "DOWN",
#      "details": {
#        "lowStockCount": 1,
#        "products": ["Coffee"]
#      }
#    }
#  }
#}
```

### 2. Vérifier la Communication Inter-Services

```powershell
# Health des services externes (ms-order)
curl http://localhost:8083/actuator/health/externalServices | jq

# Exemple de réponse :
#{
#  "status": "UP",
#  "components": {
#    "externalServices": {
#      "status": "UP",
#      "details": {
#        "userServiceHealthy": true,
#        "productServiceHealthy": true
#      }
#    }
#  }
#}
```

### 3. Logs en Temps Réel

Les logs s'affichent dans le terminal où le service est démarré.

**Niveaux de log :**
```
DEBUG : Informations détaillées pour débogage
INFO  : Messages généraux et milestones
WARN  : Avertissements (données incohérentes, etc.)
ERROR : Erreurs (exceptions, violations, etc.)
```

### 4. H2 Console (Développement)

```
URL : http://localhost:8082/h2-console (ms-product)

JDBC URL: jdbc:h2:mem:productdb
User Name: sa
Password: (laisser vide)

Cliquez sur "Connect" pour explorer la BD
```

---

## Arrêt des Services

### Arrêt Gracieux

**Dans chaque terminal :**
```powershell
# Appuyer sur Ctrl+C
# Ou taper en PowerShell :
[Ctrl+C]

# Attendre le message :
# Shutting down...
# All workers stopped
```

### Vérifier l'Arrêt

```powershell
# Les ports doivent être libérés
curl http://localhost:8082/actuator/health
# Réponse attendue : Connection refused
```

### Nettoyage Complet

```powershell
# Supprimer les fichiers compilés
cd ms-membership/ms-membership
mvn clean

cd ../../ms-product/ms-product
mvn clean

cd ../../ms-order/ms-order
mvn clean
```

---

## Scénario de Déploiement Complet

### Script de Démarrage (batch/PowerShell)

```powershell
# startServices.ps1

# Définir le répertoire du projet
$projectDir = "C:\Users\hp\Documents\plateforme-ecommerce-microservices"

# Démarrer ms-membership
Write-Host "Démarrage ms-membership..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir\ms-membership\ms-membership' ; mvn spring-boot:run"

# Attendre un peu
Start-Sleep -Seconds 3

# Démarrer ms-product
Write-Host "Démarrage ms-product..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir\ms-product\ms-product' ; mvn spring-boot:run"

# Attendre un peu
Start-Sleep -Seconds 3

# Démarrer ms-order
Write-Host "Démarrage ms-order..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir\ms-order\ms-order' ; mvn spring-boot:run"

Write-Host "Tous les services démarrés. Vérification..."
Start-Sleep -Seconds 5

# Vérifier la disponibilité
$services = @("8080", "8082", "8083")
foreach ($port in $services) {
    try {
        $response = curl "http://localhost:$port/actuator/health" -ErrorAction SilentlyContinue
        Write-Host "Port $port : OK" -ForegroundColor Green
    } catch {
        Write-Host "Port $port : KO" -ForegroundColor Red
    }
}
```

**Utilisation :**
```powershell
.\startServices.ps1
```

---

## Checklist Pré-Déploiement

- [ ] Java 21+ installé (`java -version`)
- [ ] Maven 3.9+ installé (`mvn --version`)
- [ ] Ports 8080, 8082, 8083 libres
- [ ] Repository cloné et accessible
- [ ] Fichiers `data.sql` présents dans chaque service
- [ ] Fichiers `application.yml` correctement configurés
- [ ] Dépendances Maven téléchargées (`mvn clean install`)
- [ ] Espace disque > 1 GB
- [ ] Connexion Internet (pour première exécution Maven)

---

## Support et Contacts

**Problèmes rencontrés :**

1. Consulter la section [Dépannage](#dépannage)
2. Vérifier les logs dans le terminal de démarrage
3. Valider les ports avec `netstat -ano`
4. Nettoyer le cache Maven : `mvn clean install -U`

**Documentation additionnelle :**
- Document Architecture Technique (DAT.md) : Détails de l'architecture
- README.md : Vue d'ensemble du projet
- Code source : Commentaires Javadoc complets

---

**Deployment Guide Version 1.0**  
*Plateforme E-Commerce Microservices - TP1*  
*Dernière mise à jour : December 2024*
