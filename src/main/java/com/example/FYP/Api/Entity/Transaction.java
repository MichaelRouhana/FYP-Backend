package com.example.FYP.Api.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @NotNull
    @Size(min = 42, max = 42, message = "address must be 42 characters long")
    @NotBlank(message = "Hash cannot be blank")
    private String hash;
    @Positive(message = "amount cannot be negative or 0")
    private long amount;
    @NotNull
    @Size(min = 42, max = 42, message = "address must be 42 characters long")
    @NotBlank(message = "receiver cannot be blank")
    private String receiver;

}
