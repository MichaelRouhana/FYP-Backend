package com.example.AzureTestProject.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductionViewAllDTO {

    private String uuid;
    private String date;
    private long quantity;
    private String status;
    private String warehouseName;
    private String warehouseUUID;
    private String staffNote;
    private String finishedGoodUUID;
    private String finishedGoodName;
    private long nbItems;


}
