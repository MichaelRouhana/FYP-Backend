package com.example.AzureTestProject.Api.Model.View;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiteDeskRowView {

    private String date;
    private String client;
    private String project;
    //private List<Sale> sales;
    private BigDecimal netSales;//edey be3
    private BigDecimal costOfSales;//edey kalafeto
    private BigDecimal grossProfit;//net sales - cost of sales
    private BigDecimal percentage;//gross profit / sales
  //  private List<Expense> expenses;
    private BigDecimal net;
    private BigDecimal in;
    private BigDecimal remaining;
    private BigDecimal out;
    private BigDecimal deposit;


    public static class Expense {
        private String account;
        private BigDecimal amount;
    }

    public static class Sale {
        private String product;
        private BigDecimal amount;
    }
}
