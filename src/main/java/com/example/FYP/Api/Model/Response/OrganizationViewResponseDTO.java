package com.example.FYP.Api.Model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationViewResponseDTO {
    private String uuid;
    private String name;
    private Long member_count;
    private Long projects_count;
    private String owner;
    private BigDecimal vat;
    private BigDecimal shares;
}
