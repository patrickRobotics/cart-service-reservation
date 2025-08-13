# PromoQuoter - Cart Pricing Service

A Spring Boot REST service for calculating cart prices with pluggable promotion rules and inventory management.

## Features
The microservice implements:
- **Product Management**: Create and manage products with categories, prices, and stock levels
- **Flexible Promotions**: Pluggable promotion rules supporting:
    - Percentage discounts by category
    - Buy X Get Y free offers
    - Extensible for additional promotion types
- **Cart Operations**:
    - Generate price quotes with applied promotions
    - Confirm orders with inventory reservation
    - Idempotent order confirmation
- **Concurrency Safety**: Pessimistic locking for inventory management


## Technical Stack used

- **Java 17** with **Kotlin**
- **Spring Boot 3.2.0**
- **Spring Data JPA** with **H2** in-memory database
- **Bean Validation** for input validation
- **JaCoCo** for code coverage
- **MockK** for testing

## Quick Start

### Prerequisites
- Java 17+
- Gradle 7.5+ or Maven 3.8+

### Running the Application

```bash
# Using Gradle
./gradlew bootRun

# Using Maven
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### Database Console
Access H2 console at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:promoquoter`
- Username: `sa`
- Password: (empty)

## API Examples

### 1. Create Products

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '[{
    "name": "Gaming Laptop",
    "category": "ELECTRONICS",
    "price": 1299.99,
    "stock": 5
  }, {
    "name": "Wireless Headphones",
    "category": "ELECTRONICS", 
    "price": 199.99,
    "stock": 10
  }]'
```

### 2. Create Promotions

```bash
# 15% off Electronics category
curl -X POST http://localhost:8080/promotions \
  -H "Content-Type: application/json" \
  -d '[{
    "type": "PERCENT_OFF_CATEGORY",
    "targetCategory": "ELECTRONICS",
    "discountPercentage": 0.15,
    "priority": 10
  }]'

# Buy 2 Get 1 Free on specific product
curl -X POST http://localhost:8080/promotions \
  -H "Content-Type: application/json" \
  -d '[{
    "type": "BUY_X_GET_Y",
    "targetProductId": "your-product-id-here",
    "buyQuantity": 2,
    "getQuantity": 1,
    "priority": 5
  }]'
```

### 3. Generate Quote

```bash
curl -X POST http://localhost:8080/cart/quote \
  -H "Content-Type: application/json" \
  -d '{
    "items": [{
      "productId": "your-product-id-here",
      "qty": 2
    }],
    "customerSegment": "REGULAR"
  }'
```

### 4. Confirm Order

```bash
curl -X POST http://localhost:8080/cart/confirm \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-123" \
  -d '{
    "items": [{
      "productId": "your-product-id-here", 
      "qty": 2
    }],
    "customerSegment": "REGULAR"
  }'
```

## Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

## Design Decisions

### Concurrency Control
- **Pessimistic Locking**: Used `@Lock(LockModeType.PESSIMISTIC_WRITE)` for inventory reservation to prevent overselling
- **Idempotency**: Supports `Idempotency-Key` header to prevent duplicate orders

### Promotion Engine
- **Pluggable Rules**: Easy to add new promotion types without changing core logic
- **Priority-based**: Promotions apply in configurable priority order
- **Composable**: Multiple promotions can stack on the same cart

### Error Handling
- **Structured Responses**: Consistent error format with detailed validation messages
- **HTTP Status Codes**: Proper status codes (409 for conflicts, 404 for not found)
- **Global Exception Handler**: Centralized error handling

### Data Model
- **UUID Primary Keys**: Better for distributed systems
- **Optimistic Locking**: Version fields for data integrity
- **Proper Constraints**: Database-level validation with JPA annotations

## Assumptions

1. **Single Node**: In-memory H2 database suitable for single-node deployment
2. **Price Precision**: Monetary values use `BigDecimal` with 2 decimal places
3. **Stock Management**: Simple integer stock quantities
4. **Promotion Stacking**: Multiple promotions can apply to the same item
5. **Customer Segments**: Basic segmentation (REGULAR, PREMIUM, VIP) for future extension

## Production Considerations

For production deployment, I would consider:

- **Database**: Replacing H2 with PostgreSQL/MySQL
- **Caching**: Adding Redis for promotion rules and product data
- **Monitoring**: Adding metrics and health checks
- **Security**: Implementing authentication/authorization
- **Rate Limiting**: Protecting against abuse using an API Gateway
- **Distributed Locking**: For multi-node deployments
- **Event Sourcing**: For audit trail of price changes
- **Message Queues**: For asynchronous inventory updates