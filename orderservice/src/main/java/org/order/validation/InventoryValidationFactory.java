package org.order.validation;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InventoryValidationFactory {
    private final Map<String, BaseInventoryValidator> validatorMap;
    private static final String DEFAULT_TYPE = "default";

    public InventoryValidationFactory(List<BaseInventoryValidator> validatorList) {
        this.validatorMap = validatorList.stream()
                .collect(Collectors.toMap(BaseInventoryValidator::getType, h -> h));
    }

    public BaseInventoryValidator getValidator(String type) {
        return validatorMap.getOrDefault(type, validatorMap.get(DEFAULT_TYPE));
    }
}