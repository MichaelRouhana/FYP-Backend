package com.example.FYP.Api.Model.Filter;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleFilterDTO {
    public String poNumber;
    public String wareHouseUUID;
    public String biller;
    public String vat;
    public BigDecimal discount;
    public String shippingMethod;
    public String paymentMode;
    public String address;
    public String paymentNote;
    public Boolean processed;
    public String date;

}
