package com.example.FYP.Api.Exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;


@Data
@Builder
@AllArgsConstructor
public class ApiException {

    private ZonedDateTime zonedDateTime;
    private String message;
    private HttpStatus status;
    private int code;
    private String path;

    public int getCode() {
        return code;
    }

}