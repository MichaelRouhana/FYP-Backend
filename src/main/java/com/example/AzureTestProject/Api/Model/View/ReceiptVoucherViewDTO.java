package com.example.AzureTestProject.Api.Model.View;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiptVoucherViewDTO extends VoucherViewDTO {
    private String saleUUID;
    private String dueDate;
    private String receiptPaymentType;
}
