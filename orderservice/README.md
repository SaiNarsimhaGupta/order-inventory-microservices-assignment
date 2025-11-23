Orderservice
=========

OrderService is used to Create and manage orders, integrating with an external Inventory service to update stock levels.

1) Requirements
- Java 17 (configured in Gradle toolchain)
- Gradle

2) Build & run (development)
- Build project:

```powershell
.\gradlew.bat clean build
```

- Run the application:

```powershell
.\gradlew.bat bootRun
```

3) API (HTTP)
Base path: /order

POST /order
- Description: Place an order. The controller validates the payload, calls service logic, and returns a DTO.
- Request JSON (example):

```json
{
  "productCode": "PROD-001",
  "quantity": 5
}
```

- Success response (HTTP 201 Created):

```json
{
  "id": 1,
  "orderId": "ORD-xxxxxxx",
  "productCode": "PROD-001",
  "quantity": 5,
  "status": "CONFIRMED",
  "orderDate": "2025-11-23T12:34:56",
  "message": "Order confirmed. Remaining stock: 5",
  "success": true,
  "remainingStock": 5
}
```

4) commands for formatting, testing
```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat test
```

