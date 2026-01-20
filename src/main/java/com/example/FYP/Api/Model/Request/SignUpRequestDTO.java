package com.example.FYP.Api.Model.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = false)
public class SignUpRequestDTO {

    @NotBlank(message = "email cannot be null")
    @Email
    private String email;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
    private String password;


    @NotBlank(message = "username cannot be blank")
    @Size(min = 6, max = 20, message = "username must be between 6 and 20 characters")
    private String username;

    private MultipartFile profilePic;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country; // Required country field

}
