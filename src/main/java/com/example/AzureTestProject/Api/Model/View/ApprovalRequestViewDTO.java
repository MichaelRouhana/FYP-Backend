package com.example.AzureTestProject.Api.Model.View;


import com.example.AzureTestProject.Api.Model.Constant.ApprovalStatus;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequestViewDTO {
    private String uuid;
    private String approval;
    private ApprovalStatus status;
    private Long stepId;
}
