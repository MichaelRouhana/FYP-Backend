package com.example.FYP.Api.Exception;

public class TransactionAlreadyFound extends RuntimeException {
    public TransactionAlreadyFound(String s) {
        super(s, new Throwable(s));
    }
}
