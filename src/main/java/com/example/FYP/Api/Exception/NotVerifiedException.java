package com.example.FYP.Api.Exception;

public class NotVerifiedException extends RuntimeException {
    public NotVerifiedException(String s) {
        super(s, new Throwable(s));
    }

}
