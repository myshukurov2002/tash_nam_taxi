package com.company.expections.exp;

public class UnAuthorizedException extends RuntimeException{
    public UnAuthorizedException(String message) {
        super(message);
    }

    public UnAuthorizedException() {
    }
}