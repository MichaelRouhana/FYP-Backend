package com.example.FYP.Api.Model.View;

import lombok.Data;

@Data
public class ActivityLogViewDTO {
    private String username;
    private String action;
    private String path;
    private String httpMethod;
    private String ip;
    private String timestamp;
}
