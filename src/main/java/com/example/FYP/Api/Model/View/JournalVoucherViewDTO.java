package com.example.FYP.Api.Model.View;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JournalVoucherViewDTO extends VoucherViewDTO {
    private String transactionType;
    private String vat;
}
