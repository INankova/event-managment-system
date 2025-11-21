package com.example.event_management_system.exception;

public class NotificationServiceFeignCallException extends RuntimeException {

    public NotificationServiceFeignCallException(String message) {
        super(message);
    }
}