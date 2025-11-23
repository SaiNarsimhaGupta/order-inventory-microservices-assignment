Inventory Service
=================

Inventory service is used to manage product stock and provides inventory details.


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

API documentation

Base URL: http://localhost:8081

GET:: /inventory/{productCode}
- Description: Return inventory details for the product and its batches.
- Path parameter: productCode (string)
- Success response (HTTP 200 OK):

```json
{
  "productId": 1,
  "productCode": "PROD-001",
  "productName": "Aspirin 500mg",
  "category": "HEALTH_AND_BEAUTY",
  "totalQuantity": 200,
  "availableQuantity": 180,
  "handlerType": "STANDARD",
  "batches": [
    {
      "batchId": 1,
      "batchNumber": "ASP-BATCH-001",
      "quantity": 100,
      "expiryDate": "2025-12-01",
      "manufacturingDate": "2024-06-01",
      "status": "ACTIVE",
      "supplierName": "PharmaCorp Inc",
      "expiryInfo": null
    }
  ],
  "minimumStock": 50,
  "lowStockWarning": false,
  "message": "Stock available"
}
```

POST:: /inventory/update
- Description: Deduct stock for a product after an order is placed.
- Request JSON (example):

```json
{
  "productCode": "PROD-001",
  "quantityToDeduct": 5,
  "orderId": "ORDER-1"
}
```

- Success response (HTTP 200 OK):

```json
{
  "success": true,
  "message": "Inventory updated successfully",
  "productCode": "PROD-001",
  "orderId": "ORDER-1",
  "quantityDeducted": 5,
  "remainingQuantity": 175,
  "batchDeductions": [
    {
      "batchId": 1,
      "batchNumber": "ASP-BATCH-001",
      "quantityDeducted": 5,
      "remainingQuantity": 95,
      "newStatus": "ACTIVE"
    }
  ],
  "timestamp": "2025-11-23T12:34:56"
}
```

3) commands for formatting, testing
```powershell
./gradlew spotlessApply
./gradlew test
```
