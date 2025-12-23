package com.example.AzureTestProject.Api.Model.Constant;

public enum AccountSubType {
    CURRENT_ASSETS(new AccountType[]{
            AccountType.CASH, AccountType.BANK, AccountType.INVENTORY,
            AccountType.ACCOUNTS_RECEIVABLE, AccountType.SUPPLIES,
            AccountType.PREPAID_EXPENSE, AccountType.TOOLS,
            AccountType.VAT_PAID,
            AccountType.OTHER
    }),
    LONG_TERM_ASSETS(new AccountType[]{
            AccountType.LAND, AccountType.BUILDING, AccountType.VEHICLE,
            AccountType.OTHER,
            AccountType.EQUIPMENT,
            AccountType.MACHINERY
    }),

    CURRENT_LIABILITIES(new AccountType[]{
            AccountType.ACCOUNT_PAYABLE, AccountType.SHORT_TERM_LOANS,
            AccountType.UNEARNED_REVENUE, AccountType.ACCRUES_EXPENSE,
            AccountType.VAT_COLLECTED,
            AccountType.VAT_PAYABLE,
            AccountType.OTHER
    }),
    LONG_TERM_LIABILITIES(new AccountType[]{
            AccountType.LONG_TERM_LOANS, AccountType.NOTES_PAYABLE,
            AccountType.OTHER
    }),

    OWNER_EQUITY(new AccountType[]{
            AccountType.OWNER_DRAW, AccountType.OWNER_CONTRIBUTION, AccountType.OWNER_EQUITY,
            AccountType.OTHER
    }),
    OTHER_EQUITY(new AccountType[]{
            AccountType.COMMON_STOCKS, AccountType.RETAINED_EARNINGS,
            AccountType.OTHER
    }),

    REVENUE(new AccountType[]{
            AccountType.OTHER_SALES, AccountType.REVENUE, AccountType.SALES,
            AccountType.OTHER
    }),

    COST_OF_SALES(new AccountType[]{
            AccountType.DIRECT_MATERIALS_COST, AccountType.DIRECT_LABOR_COST,
            AccountType.MANUFACTURING_OVERHEAD, AccountType.SUB_CONTRACTING,
            AccountType.OTHER
    }),

    OPERATING_EXPENSES(new AccountType[]{
            AccountType.GENERAL_EXPENSES, AccountType.SALARIES_AND_WAGES,
            AccountType.UTILITIES, AccountType.RENT, AccountType.TELEPHONE,
            AccountType.INTERNET, AccountType.INSURRANCE,
            AccountType.MAINTENANCE_AND_REPAIRS_EXPENSES,
            AccountType.OTHER
    }),
    NON_OPERATING_EXPENSES(new AccountType[]{
            AccountType.INTEREST_EXPENSES, AccountType.DEPRECIATION,
            AccountType.INCOME_TAX_EXPENSE, AccountType.DIVIDENDS, AccountType.TAXES,
            AccountType.MAINTENANCE_AND_REPAIRS_EXPENSES,
            AccountType.OTHER
    });

    private final AccountType[] accountTypes;

    AccountSubType(AccountType[] accountTypes) {
        this.accountTypes = accountTypes;
    }

    public AccountType[] getAccountTypes() {
        return accountTypes;
    }
}
