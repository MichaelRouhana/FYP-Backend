package com.example.AzureTestProject.Api.Event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuditEvent {
    private Object old;
    private String method;
    private Object newO;

}
