package com.example.AzureTestProject.Api.Exception;

public class VerificationTokenExpired extends RuntimeException {
    public VerificationTokenExpired(String s) {
        super(s, new Throwable(s));
    }

}
