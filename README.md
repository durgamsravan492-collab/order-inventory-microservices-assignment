# Order-Inventory Microservices Assignment

A simple sample project with two Spring Boot microservices demonstrating an order -> inventory flow.

Modules
- `inventoryservice` — Inventory microservice (REST API, JPA, H2)
- `orderservice` — Order microservice (calls inventory via REST client, JPA, H2)
- `scripts/` — Helpful curl/PowerShell request sequences for manual testing

This README contains: project setup, API documentation for both services, and testing instructions.

---

## Prerequisites
- Java 17+ (or the Java version used by the project)
- Maven 3.6+

---

## Project setup (build & run)
From the repository root you can build the project and run each service independently.

Build everything:

    mvn clean install

Run Inventory service only (default binding used in examples below):

    mvn -pl inventoryservice spring-boot:run

Run Order service only:

    mvn -pl orderservice spring-boot:run

Notes:
- Example service ports used in this README and `scripts/inventory_update_requests.txt`:
  - Inventory service: `http://localhost:8081`
  - Order service: `http://localhost:8082`
- If you need to change ports or other properties, see each module's `src/main/resources/application.yml`.

---

## API documentation

This section documents the public REST endpoints for the two services and shows example request bodies and commands.

Inventory service (base: http://localhost:8081)

- POST /inventory/product
  - Description: Create a Product
  - Request body:
    {
      "sku": "SKU-1",
      "name": "Product name"
    }
  - Response: 201 Created with Product JSON (includes `id`)
  - Example (curl):

      curl -X POST "http://localhost:8081/inventory/product" -H "Content-Type: application/json" -d '{"sku":"SKU-1","name":"Product name"}'

- POST /inventory/batch?productId={productId}
  - Description: Create an inventory batch for a product ID
  - Request body:
    {
      "batchNumber": "BATCH-1",
      "quantity": 5,
      "expiryDate": "2025-12-31"
    }
  - Response: 201 Created with InventoryBatchDto
  - Example (curl):

      curl -X POST "http://localhost:8081/inventory/batch?productId=1" -H "Content-Type: application/json" -d '{"batchNumber":"BATCH-1","quantity":5,"expiryDate":"2025-12-31"}'

- GET /inventory/batches?sku={sku}
  - Description: List all batches for the given SKU
  - Response: 200 OK with JSON array of InventoryBatchDto
  - Example (curl):

      curl "http://localhost:8081/inventory/batches?sku=SKU-1"

- POST /inventory/update?handlerType={handler}
  - Description: Deduct quantities from specific batches for a SKU. Used by the Order service after allocation.
  - Query param: `handlerType` (default: `default`)
  - Request body shape (required):
    {
      "sku": "SKU-1",
      "batchQuantityToDeduct": {
        "BATCH-1": 3,
        "BATCH-2": 1
      }
    }
  - Validations and error cases handled by the service (examples):
    - Missing body or missing SKU -> validation error
    - Missing or empty `batchQuantityToDeduct` -> validation error
    - Batch not found for SKU -> validation error
    - Insufficient qty in batch -> validation error
  - Response: 200 OK on success (empty body)
  - Example (curl):

      curl -X POST "http://localhost:8081/inventory/update?handlerType=default" -H "Content-Type: application/json" -d '{"sku":"SKU-1","batchQuantityToDeduct":{"BATCH-1":3}}'

Order service (base: http://localhost:8082)

- POST /order
  - Description: Place an order. The service will validate the request, fetch available batches from the Inventory service, allocate quantities (via InventoryAllocator), call the inventory update validator, persist the Order, and return the created order details.
  - Request body:
    {
      "sku": "SKU-1",
      "quantity": 2
    }
  - Response: 200 OK with OrderResponse JSON containing:
    - `success` (boolean)
    - `order` (Order entity when success)
    - `error` (error message when not successful)
  - Example (curl):

      curl -X POST "http://localhost:8080/order" -H "Content-Type: application/json" -d '{"sku":"SKU-1","quantity":2}'

Important: The Order service depends on the Inventory service to fetch batches and to update inventory. Start the Inventory service before placing orders.

---

## Testing

Run all tests for the project (recommended from repository root):

    mvn -pl inventoryservice test
    mvn -pl orderservice test

Or run both modules' tests (single command):

    mvn -DskipTests=false test

There are integration tests included under each module (see `src/test/java` in each service). Use your IDE or Maven to run them.

---

## Troubleshooting
- Ports: If the services don't respond, confirm they are running and bound to the expected port (8081 for inventory, 8082 for order). Adjust `application.yml` in each module if needed.

---

