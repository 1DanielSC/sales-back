package com.salesback.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends jakarta.persistence.EntityNotFoundException{
    
    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }
}
