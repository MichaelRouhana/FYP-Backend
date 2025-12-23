package com.example.FYP.Api.ExceptionHandler;

import com.example.FYP.Api.Exception.TransactionAlreadyFound;
import com.example.FYP.Api.Exception.TransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class TransactionExceptionHandler {
    @ExceptionHandler(TransactionAlreadyFound.class)
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<Object> handleTransactionAlreadyFound(TransactionAlreadyFound ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        var exception = new TransactionException(ex.getMessage(), ex.getCause(), HttpStatus.FOUND, path);
        return new ResponseEntity<>(exception, HttpStatus.FOUND);
    }

}
