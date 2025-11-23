package com.inventory.validation;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InventoryValidationFactory {
    private final Map<String, BaseInventoryValidator> validatorMap;

    public static final String INVALID_TYPE = "invalid type";

    public InventoryValidationFactory(List<BaseInventoryValidator> validatorList) {
        this.validatorMap = validatorList.stream()
                .collect(Collectors.toMap(BaseInventoryValidator::getType, h -> h));
    }

    public BaseInventoryValidator getValidator(String type) {
        if (type == null || !validatorMap.containsKey(type)) {
            throw new IllegalArgumentException(INVALID_TYPE);
        }
        return validatorMap.get(type);
    }
}
