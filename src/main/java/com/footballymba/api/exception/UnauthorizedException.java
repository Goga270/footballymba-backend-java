package com.footballymba.api.exception;

// Ручное исключение для ошибок авторизации
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}