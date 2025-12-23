package com.example.FYP.Api.Model.View;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class MaintenanceViewDTO {
    private Long id;
    private String status;
    private String assetUUID;
    private String startedBy;
    private List<JsonNode> steps;
}
