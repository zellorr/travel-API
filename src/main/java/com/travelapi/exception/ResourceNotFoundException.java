package com.travelapi.exception;

public class ResourceNotFoundException extends RuntimeException {

    private String resourceType;
    private long resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, long resourceId) {
        super(String.format("%s with ID %d not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getResourceType() {
        return resourceType;
    }

    public long getResourceId() {
        return resourceId;
    }
}
