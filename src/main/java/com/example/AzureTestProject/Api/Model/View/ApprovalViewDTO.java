package com.example.AzureTestProject.Api.Model.View;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalViewDTO {

    private String uuid;
    private String status;
    private String entity;
    private String entityId;
    private List<ApprovalStepViewDTO> steps;

    @Data
    public static class ApprovalStepViewDTO {
        private String status;
        private String order;
        private String user = "";
        private boolean end;
        private String tier;
        private String role;


    }
}
