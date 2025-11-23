package com.inventory.handlers;

public class InvalidInventoryOperationException extends RuntimeException {
    public InvalidInventoryOperationException(String message) {
        super(message);
    }
}

