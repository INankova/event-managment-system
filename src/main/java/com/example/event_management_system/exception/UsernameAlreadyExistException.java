package com.example.event_management_system.exception;

public class UsernameAlreadyExistException extends RuntimeException{

    public UsernameAlreadyExistException(String message) {
        super(message);
    }

    public UsernameAlreadyExistException() {
    }
}
