package com.example.AzureTestProject.Api.Exception;

public class UserAlreadyInvitedException extends RuntimeException {
    public UserAlreadyInvitedException(String s) {
        super(s, new Throwable(s));
    }

}
