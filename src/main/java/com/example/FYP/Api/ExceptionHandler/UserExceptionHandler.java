package com.example.FYP.Api.ExceptionHandler;

import com.example.FYP.Api.Exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;

@ControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiException> handleException(UserAlreadyExistException ex, WebRequest request) {
        ErrorResponse t;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiException exception = ApiException.builder()
                .status(HttpStatus.CONFLICT)
                .message(ex.getMessage())
                .zonedDateTime(ZonedDateTime.now())
                .code(HttpStatus.CONFLICT.value())

                .path(path)
                .build();
        return new ResponseEntity<>(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotVerifiedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiException> handleException(NotVerifiedException ex, WebRequest request) {
        ErrorResponse t;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiException exception = ApiException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .path(path)
                .build();
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationTokenExpired.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiException> handleException(VerificationTokenExpired ex, WebRequest request) {
        ErrorResponse t;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiException exception = ApiException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .zonedDateTime(ZonedDateTime.now())
                .path(path)
                .build();
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiException> handleException(UserNotFoundException ex, WebRequest request) {
        ErrorResponse t;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiException exception = ApiException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .code(HttpStatus.BAD_REQUEST.value())
                .zonedDateTime(ZonedDateTime.now())
                .path(path)
                .build();
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);
    }


}