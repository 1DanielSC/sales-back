package com.salesback.exceptions;

public class GenericException extends RuntimeException{
    public GenericException(){}

    public GenericException(String message) {
        super(message);
    }
}
