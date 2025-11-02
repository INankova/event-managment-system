package com.example.event_management_system.exception;

public class VenueNameAlreadyExistsException extends RuntimeException{

    public VenueNameAlreadyExistsException(String message) {
        super(message);
    }

    public VenueNameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}