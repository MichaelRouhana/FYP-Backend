package com.example.AzureTestProject.Api.Exception;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String s) {
        super(s, new Throwable(s));
    }

}
