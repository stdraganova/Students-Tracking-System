package com.rewe.studentstrackingsystem.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException of(String resource, String identifier) {
        return new ResourceNotFoundException(resource + " not found with id: " + identifier);
    }
}

