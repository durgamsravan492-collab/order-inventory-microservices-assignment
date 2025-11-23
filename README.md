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

This section documents the public REST endpoints for the two services and shows example curl requests.

Inventory service (base: http://localhost:8081)
![screencapture-localhost-8081-swagger-ui-index-html-2025-11-23-16_49_31.png](../../Sceenshots/screencapture-localhost-8081-swagger-ui-index-html-2025-11-23-16_49_31.png)

1. http://localhost:8081/inventory/product

postman request POST 'http://localhost:8081/inventory/product' \
--header 'Content-Type: application/json' \
--body '{
"sku": "SKU-1",
"name": "Test Product"
}'
![1.Create Product.png](../../Sceenshots/1.Create%20Product.png)

2. http://localhost:8081/inventory/batch?productId=1


postman request POST 'http://localhost:8081/inventory/batch?productId=1' \
--header 'Content-Type: application/json' \
--body '{
"batchNumber": "BATCH-1",
"quantity": 5,
"expiryDate": "2025-12-31"
}'
![2.Create Batch.png](../../Sceenshots/2.Create%20Batch.png)

3. http://localhost:8081/inventory/update?handlerType=default


postman request POST 'http://localhost:8081/inventory/update?handlerType=default' \
--header 'Content-Type: application/json' \
--body '{
"sku": "SKU-1",
"batchQuantityToDeduct": {
"BATCH-1": 3
}
}

![4.Inventory Update.png](../../Sceenshots/4.Inventory%20Update.png)

4. postman request GET
   http://localhost:8081/inventory/batches?sku=SKU-1

![5.Batch By.png](../../Sceenshots/5.Batch%20By.png)

5. postman request GET
   http://localhost:8081/inventory/batches

![6.Batches.png](../../Sceenshots/6.Batches.png)

Order service (base: http://localhost:8082)
![screencapture-localhost-8082-swagger-ui-index-html-2025-11-23-16_49_47.png](../../Sceenshots/screencapture-localhost-8082-swagger-ui-index-html-2025-11-23-16_49_47.png)

1. http://localhost:8082/order

postman request POST 'http://localhost:8082/order' \
--header 'Content-Type: application/json' \
--body '{
"sku": "SKU-1",
"quantity": 2
}'
![3.Place Order.png](../../Sceenshots/3.Place%20Order.png)

Important: The Order service depends on the Inventory service to fetch batches and to update inventory. Start the Inventory service before placing orders.

DB Validation:
Connection details (copy these into the H2 web console login form)

- Inventory JDBC URL:

  jdbc:h2:mem:inventorydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE

- Order JDBC URL:

  jdbc:h2:mem:orderdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE

- User: sa
- Password: (leave blank)

![Products Table.png](../../Sceenshots/Products%20Table.png)
![Inventory Batches Table.png](../../Sceenshots/Inventory%20Batches%20Table.png)
![Orders Table.png](../../Sceenshots/Orders%20Table.png)

Postman Collection:
[Order Inventory Collection.postman_collection.json](../../Sceenshots/Order%20Inventory%20Collection.postman_collection.json)

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

