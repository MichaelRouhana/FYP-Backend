package com.example.AzureTestProject.Api.Exception;

public class TransactionAlreadyFound extends RuntimeException {
    public TransactionAlreadyFound(String s) {
        super(s, new Throwable(s));
    }
}
