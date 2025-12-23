package com.example.FYP.Api.Model.View;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentVoucherViewDTO extends VoucherViewDTO {
    private String paymentType;
    private String voucher;
}
