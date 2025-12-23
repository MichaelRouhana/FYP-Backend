package com.example.AzureTestProject.Api.Model.View;

import com.example.AzureTestProject.Api.Model.Constant.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class    VoucherViewDTO {
    private VoucherType type;
    private String uuid;
    private String date;
    private boolean voided;
    private boolean returned;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private List<AccountEntryViewDTO> accountEntries;
    private String note;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountEntryViewDTO {
        private String name;
        private BigDecimal debit;
        private BigDecimal credit;
    }


}
