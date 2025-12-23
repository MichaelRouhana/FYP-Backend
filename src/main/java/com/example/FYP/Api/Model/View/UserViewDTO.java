package com.example.FYP.Api.Model.View;

import lombok.Data;

import java.util.List;

@Data
public class UserViewDTO {
    private String username;
    private String email;
    private String pfp;
    private List<String> roles;
}
