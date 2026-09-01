# Cart Service

The service manages user-specific shopping carts within the E-Commerce Microservices architecture. It provides authenticated cart operations, quantity-aware add-to-cart functionality, cart updates and item removal, and checkout integration with the Order Service. User identity is obtained through the User & Auth Service and stored in the Spring Security context, ensuring that cart data is always accessed in the context of the authenticated user.

> **Note:** This service is part of a larger E-Commerce Microservices project.

---

## Highlights

* **Authenticated cart access** — All cart operations require an authenticated user, ensuring users can access only their own cart
* **User-specific cart ownership** — The authenticated user's ID is obtained from the User & Auth Service and stored in the Spring Security context rather than being trusted from the client request
* **Quantity-aware Add to Cart** — Repeatedly adding the same product updates its existing cart quantity instead of creating duplicate cart items
* **Dedicated cart update flow** — Cart updates from the cart screen are handled separately from the Add to Cart operation, allowing quantities and other supported cart details to be modified explicitly
* **Soft-delete cart lifecycle** — After an order is successfully created, the cart is marked inactive instead of being immediately deleted
* **Persistent active carts** — Active carts remain available across sessions so users can return later and continue shopping
* **Checkout orchestration** — Checkout delegates order creation to the Order Service rather than directly handling order or payment responsibilities
* **Service-to-service communication** — Communication with User & Auth and Order Services uses Eureka-based service discovery and load-balanced RestTemplate
* **Clean layered architecture** — Controller, service, repository, security, and domain responsibilities are separated following clean code and Low-Level Design principles

---

## Architecture Overview

```text
                              Client
                                │
                                ▼
                         Cart Service
                                │
                                ▼
                    Spring Security Filter Chain
                                │
                                ▼
                    User/Auth Service /validate
                                │
                         Access Token
                                │
                                ▼
                         X-User-Id Header
                                │
                                ▼
                         SecurityContext
                                │
                 ┌──────────────┼──────────────┐
                 │              │              │
                 ▼              ▼              ▼
              GET /cart     POST /cart      PUT /cart
                 │              │              │
                 │              │              │
                 └──────────────┼──────────────┘
                                │
                                ▼
                         Cart Service Layer
                                │
                                ▼
                         Cart Repository
                                │
                                ▼
                            MySQL DB


                         CHECKOUT FLOW

                         POST /cart/checkout
                                │
                                ▼
                          Order Service
                                │
                                ▼
                         Payment Service
                                │
                                ▼
                           Payment Link
                                │
                                ▼
                          Order Response
                                │
                                ▼
                    Cart → isActive = false
```

---

## Authentication & Authorization

The Cart Service requires authentication for **all endpoints** because cart data is user-specific.

Unlike the Product Catalog, where browsing can be public, a user's cart should only be accessible to that authenticated user.

The Cart Service uses the authenticated user's ID from the security context when accessing cart data.

The client does not provide the user ID as the source of truth for cart ownership.

---

## Cart Features

### View Cart

```text
GET /cart
```

The authenticated user's ID is retrieved from the security context and used to locate the user's active cart.

```text
GET /cart
     │
     ▼
Spring Security Filter Chain
     │
     ▼
Validate Access Token
     │
     ▼
User/Auth Service /validate
     │
     ▼
X-User-Id
     │
     ▼
SecurityContext
     │
     ▼
Cart Service
     │
     ▼
Find Active Cart
     │
     ▼
Return Cart
```
---

### Add to Cart

```text
POST /cart/
```

This endpoint represents the **Add to Cart** action commonly found on e-commerce platforms.

If a product is added for the first time:

```text
Product A
Quantity = 1
```

If the same product is added again:

```text
Product A
Quantity = 2
```

and so on.

The operation therefore updates the existing cart item's quantity when the product is already present rather than creating duplicate cart items.

```text
POST /cart/
     │
     ▼
Authenticated User
     │
     ▼
SecurityContext → User ID
     │
     ▼
Find Active Cart
     │
     ▼
Check Product
     │
     ├── New Product
     │       └── Add Cart Item
     │
     └── Existing Product
             └── Increase Quantity
```
---

### Update Cart

```text
PUT /cart/
```

This endpoint is intended for modifications made from the cart screen.

For example:

```text
Product A → Quantity 1
          ↓
Product A → Quantity 3
```

It provides a separate update flow from the Add to Cart operation.

```text
PUT /cart/
     │
     ▼
Authenticated User
     │
     ▼
SecurityContext → User ID
     │
     ▼
Find User's Active Cart
     │
     ▼
Update Cart Item
     │
     ▼
Persist Changes
```
---

### Remove Cart Item

```text
DELETE /cart/{id}
```

Removes the specified item from the authenticated user's cart.

---

## Checkout

Checkout is responsible for initiating the transition from a shopping cart to an order.

```text
POST /cart/checkout
```

The Cart Service does not directly create the payment.

Instead, the flow is:

```text
POST /cart/checkout
        │
        ▼
Authenticated User
        │
        ▼
Retrieve Active Cart
        │
        ▼
Call Order Service
        │
        ▼
Order Service
        │
        ▼
Payment Service
        │
        ▼
Payment Link
        │
        ▼
Order Created
        │
        ▼
Cart.isActive = false
        │
        ▼
Return Checkout Information
```

Once the Order Service successfully creates the order, the Cart Service marks the corresponding cart as inactive.

---

## Cart Lifecycle

The cart follows an active/inactive lifecycle.

### Active Cart

An active cart remains available even if the user leaves the application or logs out.

```text
User adds products
        │
        ▼
   Active Cart
        │
        ├── User leaves
        │
        └── User returns later
                │
                ▼
          Cart still available
```

This allows users to continue shopping without losing previously added items.

### After Checkout

After successful order creation:

```text
Active Cart
     │
     ▼
Order Created
     │
     ▼
isActive = false
```

The cart is therefore no longer presented as the user's current active cart.

---

## Soft Delete

Carts that have successfully transitioned into orders are **soft-deleted** by setting:

```text
isActive = false
```

The cart is not immediately physically deleted.

At the end of the day, a scheduled cleanup job can remove inactive carts because their contents have already been transferred into the corresponding order.

```text
Checkout
   │
   ▼
Order Created
   │
   ▼
Cart.isActive = false
   │
   │
   │ EOD Scheduled Job(In Roadmap)
   ▼
Physical Deletion
```

Active carts are not affected by the cleanup process.

```text
Active Cart
   │
   ├── No checkout
   ├── User leaves
   └── User returns next day
             │
             ▼
       Cart remains active
```

---

## API Endpoints

The service uses `/cart` as its context path.

All endpoints require authentication.

| Method   | Endpoint         | Auth Required | Description                                      |
| -------- | ---------------- | ------------- | ------------------------------------------------ |
| `GET`    | `/cart`          | Access Token  | Retrieve the authenticated user's active cart    |
| `POST`   | `/cart/`         | Access Token  | Add an item to the cart or increase its quantity |
| `PUT`    | `/cart/`         | Access Token  | Update cart item details/quantities              |
| `DELETE` | `/cart/{id}`     | Access Token  | Remove an item from the cart                     |
| `POST`   | `/cart/checkout` | Access Token  | Create an order from the current cart            |

---

## Service-to-Service Communication

The Cart Service communicates with the User & Auth Service and Order Service using service discovery and load-balanced HTTP communication.

```text
                     Eureka
                  Service Registry
                  /             \
                 /               \
                ▼                 ▼
        User/Auth Service     Order Service
                ▲                 ▲
                │                 │
                │                 │
          /validate           Create Order
                ▲                 ▲
                │                 │
                └───────┬─────────┘
                        │
                        │
                  Cart Service
                        │
               LoadBalanced
                 RestTemplate
```

### User/Auth Communication

Used to validate the access token and obtain the authenticated user's identity.

```text
Cart Service
     │
     ▼
LoadBalanced RestTemplate
     │
     ▼
User/Auth Service
     │
     ▼
/validate
```

### Order Communication

Used during checkout to create an order from the user's active cart.

```text
Cart Service
     │
     ▼
LoadBalanced RestTemplate
     │
     ▼
Order Service
```

The physical location of these services is not hardcoded; they are resolved through service discovery.

---

## Design Decisions

### Why require authentication for every cart operation?

-> A cart is user-specific data. Unlike product browsing, where public access is desirable, users should only be able to view and modify their own cart. Requiring authentication provides a trusted user context for every cart operation.

### Why store the user ID in SecurityContext?

-> The authenticated user identity is established by the User & Auth Service. Storing the returned user ID in the SecurityContext allows the Cart Service to consistently use the authenticated identity throughout the request without accepting a client-provided user ID as the source of truth.

### Why delegate authentication to User/Auth Service?

-> Authentication is centralized in the User & Auth Service. The Cart Service does not duplicate JWT validation and token management logic; instead, it delegates token validation through the `/validate` endpoint and uses the returned user identity.

### Why distinguish Add to Cart from Update Cart?

-> The Add to Cart operation represents the product-listing experience where repeatedly adding the same product increases its quantity. The Update Cart operation represents modifications made from the cart screen and provides an explicit update flow for quantities and other supported cart properties.

### Why soft-delete the cart after checkout?

-> Once a cart has been successfully converted into an order, it should no longer appear as the user's active cart. Marking it inactive separates the business lifecycle from physical database cleanup while avoiding unnecessary immediate deletion. At end-of-day a cron job can remove these records while keeping the checkout operation itself simple and separating business state transition from database cleanup.

### Why retain active carts?

-> Users may leave the application and return later expecting their cart to still contain their previously selected products. Active carts therefore remain persistent across sessions until they are successfully converted into an order.

### Why does Cart call Order instead of Payment directly?

-> The Order Service owns order creation and order state, while the Payment Service owns payment processing. The Cart Service initiates checkout by asking the Order Service to create the order, allowing the Order Service to coordinate the subsequent payment workflow.

---

## Tech Stack

| Layer                 | Technology                 |
| --------------------- | -------------------------- |
| Framework             | Spring Boot                |
| Security              | Spring Security            |
| Database              | MySQL                      |
| ORM                   | Spring Data JPA            |
| Service Discovery     | Netflix Eureka             |
| Service Communication | Load-balanced RestTemplate |
| Build Tool            | Maven                      |
| Language              | Java                       |

---

## Environment Variables

Sensitive database and service configuration should be externalized through environment variables rather than hardcoded.

| Variable              | Description         |
| --------------------- | ------------------- |
| `DATASOURCE_URL`      | JDBC connection URL |
| `DATASOURCE_USERNAME` | Database username   |
| `DATASOURCE_PASSWORD` | Database password   |
| `EUREKA_SERVER_URL`   | Eureka Server URL   |

---
<!--
## Getting Started

```bash
# Clone the repository
git clone https://github.com/your-username/cart-service.git

# Navigate to the project
cd cart-service

# Configure environment variables

# Run the service
./mvnw spring-boot:run
```

The Cart Service will register itself with the Eureka Service Discovery Server and communicate with the User & Auth and Order Services through service discovery.

---
-->

## Known Gaps & Roadmap

* Kafka-based event communication
* Event-driven order/cart lifecycle updates
* Redis caching for frequently accessed carts
* Scheduled cleanup of inactive carts
* Distributed tracing and observability
* Docker containerization
* Additional cart optimization and concurrency handling
