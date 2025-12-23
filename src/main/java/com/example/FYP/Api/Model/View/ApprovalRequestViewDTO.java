package com.example.FYP.Api.Model.View;


import com.example.FYP.Api.Model.Constant.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
