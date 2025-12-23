package com.example.FYP.Api.Exception;

public class VerificationTokenExpired extends RuntimeException {
    public VerificationTokenExpired(String s) {
        super(s, new Throwable(s));
    }

}
