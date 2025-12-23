package com.example.FYP.Api.Model.Constant;

public enum AccountMainType {

    ASSETS(new AccountSubType[]{
            AccountSubType.CURRENT_ASSETS,
            AccountSubType.LONG_TERM_ASSETS
    }),

    LIABILITIES(new AccountSubType[]{
            AccountSubType.CURRENT_LIABILITIES,
            AccountSubType.LONG_TERM_LIABILITIES
    }),

    EQUITY(new AccountSubType[]{
            AccountSubType.OWNER_EQUITY,
            AccountSubType.OTHER_EQUITY,
    }),

    REVENUE(new AccountSubType[]{
            AccountSubType.REVENUE,
            AccountSubType.COST_OF_SALES,
    }),

    EXPENSES(new AccountSubType[]{
            AccountSubType.OPERATING_EXPENSES,
            AccountSubType.NON_OPERATING_EXPENSES,
    });


    private final AccountSubType[] subAccountTypes;

    // Constructor to associate subtypes with the main type
    AccountMainType(AccountSubType[] subAccountTypes) {
        this.subAccountTypes = subAccountTypes;
    }

    // Getter to retrieve associated subtypes
    public AccountSubType[] getSubAccountTypes() {
        return subAccountTypes;
    }
}
