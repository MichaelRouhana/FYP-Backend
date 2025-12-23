package com.example.FYP.Api.Exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
@Builder
public class TransactionException {
    private String message;
    private Throwable error;
    private HttpStatus status;
    private String path;

    public TransactionException(String message, Throwable cause, HttpStatus status, String path) {
        this.message = message;
        this.error = cause;
        this.status = status;
        this.path = path;
    }

}