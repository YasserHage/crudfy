package com.crudfy.domains.exceptions;

public class ResourceValidationException extends RuntimeException{
    public ResourceValidationException(String message) {
        super(message);
    }
}
