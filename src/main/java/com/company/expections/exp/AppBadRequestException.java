package com.company.expections.exp;

public class AppBadRequestException extends RuntimeException {
    public AppBadRequestException(String message) {
        super(message);
    }
    public AppBadRequestException() {
        super("App Bad Request Exception!");
    }
}
