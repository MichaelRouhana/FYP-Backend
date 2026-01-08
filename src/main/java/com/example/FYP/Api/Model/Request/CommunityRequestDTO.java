package com.example.FYP.Api.Model.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityRequestDTO {

    @NotBlank(message = "Community name is required")
    private String name;
    
    private String shortDescription; // Optional, 1-2 lines
    
    @NotBlank(message = "Community description is required")
    private String description; // Required, detailed text
    
    @Builder.Default
    private Boolean isPrivate = false; // Default false
    
    private String logoUrl; // Optional (maps to logo in entity)
    
    // Legacy fields for backward compatibility
    private String logo; // Maps to logoUrl if provided
    private String location;
    private String about; // Maps to description if provided
    private List<String> rules;
}
