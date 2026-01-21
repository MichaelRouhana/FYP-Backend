package com.example.FYP.Api.Model.Filter;

import lombok.Data;

@Data
public class ActivityLogFilterDTO {
    private String username;
    private String action;
    private String path;
    private String httpMethod;
    private String ip;
    private String timestamp;
}
