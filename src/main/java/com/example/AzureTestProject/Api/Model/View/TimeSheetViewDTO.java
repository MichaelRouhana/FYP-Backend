package com.example.AzureTestProject.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSheetViewDTO {
    private String date;
    private String name;
    private List<EmployeeViewDTO> employees;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployeeViewDTO {
        private String uuid;
        private String name;
        private String checkIn;
        private String checkOut;
        private String overtime;
        private Double overtimeRate;
        private String task;
        private String taskDescription;
    }
}
