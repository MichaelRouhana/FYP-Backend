package com.example.AzureTestProject.Api.Model.Request;

import com.example.AzureTestProject.Api.Model.Constant.ProjectStatus;
import com.example.AzureTestProject.Api.Validation.Annotation.EnumValidator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectRequestDTO {

    @NotBlank(message = "name cannot be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "type cannot be blank")
    @Size(min = 3, max = 50, message = "Type must be between 3 and 50 characters")
    private String type;

    @NotBlank(message = "Project status cannot be null")
    @EnumValidator(
            enumClass = ProjectStatus.class,
            message = "Invalid projectStatus. Allowed values are:"
    )
    private String projectStatus;


    @NotBlank(message = "leadUUID cannot be blank")
    private String leadUUID;


    private String attachment;

    @Size(min = 10, max = 200, message = "description must be between 10 and 200 characters")
    private String description;

    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must not exceed 100")
    private int progress;

    private Set<Long> userIds;
}
