package com.example.FYP.Api.Exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiRequestException extends RuntimeException {

    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }


    public static ApiRequestException badRequest(String message) {
        return new ApiRequestException(HttpStatus.BAD_REQUEST, message);
    }
}