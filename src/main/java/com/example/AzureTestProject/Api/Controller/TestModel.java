package com.example.AzureTestProject.Api.Controller;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestModel {

    /*@ValidEnum(enumClass = StatusEnum.class)
    private StatusEnum status;*/

    @NotBlank(message = "test cannot be blank")
    private String test;


    public enum StatusEnum {
        ACTIVE, INACTIVE, PENDING, TEST
    }

}
