package com.example.AzureTestProject.Api.Model.View;

import com.example.AzureTestProject.Api.Model.Constant.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialRequisiteViewAllDTO {
    private String uuid;
    private Set<String> categories;
    private Set<String> subCategories;
    private String date;
    private String requiredDate;
    private Priority priority;
    private boolean processed;
    private String note;

}
