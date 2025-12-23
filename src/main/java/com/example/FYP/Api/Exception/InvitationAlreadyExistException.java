package com.example.FYP.Api.Exception;

public class InvitationAlreadyExistException extends RuntimeException {
    public InvitationAlreadyExistException(String s) {
        super(s, new Throwable(s));
    }

}
