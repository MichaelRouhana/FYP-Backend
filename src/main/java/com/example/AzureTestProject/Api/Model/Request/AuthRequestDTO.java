package com.example.AzureTestProject.Api.Model.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = false)
public class AuthRequestDTO {

    @NotBlank(message = "email cannot be null")
    @Email
    private String email;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
    private String password;
}

