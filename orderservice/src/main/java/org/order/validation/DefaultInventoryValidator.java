package org.order.validation;


import org.order.dto.InventoryUpdateRequest;
import org.order.downstream.InventoryClient;
import org.springframework.stereotype.Component;

@Component
public class DefaultInventoryValidator implements BaseInventoryValidator {

     private final InventoryClient inventoryClient;
     private static final String DEFAULT_TYPE = "default";

     public DefaultInventoryValidator(InventoryClient inventoryClient) {
         this.inventoryClient = inventoryClient;
     }

     @Override
     public String getType() {
         return DEFAULT_TYPE;
     }

     @Override
     public void updateInventory(InventoryUpdateRequest inventoryUpdateRequest) {
         inventoryClient.updateInventory(inventoryUpdateRequest);
     }
 }
