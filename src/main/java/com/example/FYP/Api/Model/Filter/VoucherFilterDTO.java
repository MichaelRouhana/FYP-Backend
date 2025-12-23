package com.example.FYP.Api.Model.Filter;

import com.example.FYP.Api.Model.Constant.VoucherType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VoucherFilterDTO {
    private LocalDate date;
    private String uuid;
    private VoucherType type;
    private Boolean voided;
    private Boolean returned;
    private Boolean processed;
}
