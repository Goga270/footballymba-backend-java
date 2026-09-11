package com.footballymba.api.exception;

// Ручное исключение для ситуаций "не найдено в БД"
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}