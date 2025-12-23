package com.example.FYP.Api.Model.Response;

import lombok.Data;

@Data
public class ProjectResponseDTO {

    private String uuid;
    private String name;
    private String type;
    private String projectStatus;
    private String attachment;
    private String description;
}
