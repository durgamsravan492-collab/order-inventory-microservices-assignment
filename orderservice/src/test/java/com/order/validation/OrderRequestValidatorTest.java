package com.order.validation;

import com.order.dto.OrderRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderRequestValidatorTest {

    private static final String SKU_1 = "SKU-1";
    private static final String BLANK = "   ";

    private final OrderRequestValidator validator = new OrderRequestValidator();

    @Test
    void validate_nullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
    }

    @Test
    void validate_blankSkuThrows() {
        OrderRequest r = new OrderRequest();
        r.setSku(BLANK);
        r.setQuantity(1);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(r));
    }

    @Test
    void validate_zeroQuantityThrows() {
        OrderRequest r = new OrderRequest();
        r.setSku(SKU_1);
        r.setQuantity(0);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(r));
    }

    @Test
    void validate_negativeQuantityThrows() {
        OrderRequest r = new OrderRequest();
        r.setSku(SKU_1);
        r.setQuantity(-5);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(r));
    }

    @Test
    void validate_validRequestPasses() {
        OrderRequest r = new OrderRequest();
        r.setSku(SKU_1);
        r.setQuantity(3);
        validator.validate(r);
    }
}
