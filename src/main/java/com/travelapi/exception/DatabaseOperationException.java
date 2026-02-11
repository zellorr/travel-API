package com.travelapi.exception;

public class DatabaseOperationException extends RuntimeException {

    private String operation;
    private String entity;

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String operation, String entity, Throwable cause) {
        super(String.format("Database operation '%s' failed for entity '%s': %s", operation, entity, cause.getMessage()), cause);
        this.operation = operation;
        this.entity = entity;
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getOperation() {
        return operation;
    }

    public String getEntity() {
        return entity;
    }
}
