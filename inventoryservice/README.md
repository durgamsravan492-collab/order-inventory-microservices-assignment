# Inventory Service

Run:

mvn -pl inventoryservice spring-boot:run

Endpoints:
- GET /inventory/{productId}
- GET /inventory/batches?sku={sku}
- POST /inventory/update
- POST /inventory/product
- POST /inventory/batch

Tests:

mvn -pl inventoryservice test

