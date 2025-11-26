package com.proyect_planning.proyect_planning_system.services.cloud.exceptions;

public class CloudException extends Exception {
    public CloudException(String message) {
        super(message);
    }

    public CloudException(String message, Throwable cause) {
        super(message, cause);
    }
}
