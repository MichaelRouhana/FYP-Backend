package com.example.FYP.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogViewDTO {
    private Long id;
    private String action;
    private String details;
    private String username;
    private LocalDateTime timestamp;
}

