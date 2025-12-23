package com.example.FYP.Api.Exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String s) {
        super(s, new Throwable(s));
    }

}
