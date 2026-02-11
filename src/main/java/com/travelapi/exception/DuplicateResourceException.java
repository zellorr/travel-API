package com.travelapi.exception;

public class DuplicateResourceException extends InvalidInputException {

    private String resourceType;
    private String conflictingField;

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceType, String conflictingField) {
        super(String.format("Duplicate %s found with %s", resourceType, conflictingField));
        this.resourceType = resourceType;
        this.conflictingField = conflictingField;
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getConflictingField() {
        return conflictingField;
    }
}
