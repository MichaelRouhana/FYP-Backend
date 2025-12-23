package com.example.FYP.Api.Model.Request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvitationRequestDTO {

    @NotBlank(message = "inviteeEmail cannot be null")
    @Email
    private String inviteeEmail;
}
