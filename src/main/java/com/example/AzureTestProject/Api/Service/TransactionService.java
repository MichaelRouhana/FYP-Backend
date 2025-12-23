package com.example.AzureTestProject.Api.Service;


import com.example.AzureTestProject.Api.Entity.Transaction;
import com.example.AzureTestProject.Api.Exception.TransactionAlreadyFound;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class TransactionService {
    private final HashSet<Transaction> transactions = new HashSet<>();

    public Optional<Transaction> save(Transaction transaction) {
        if (transactions.contains(transaction)) throw new TransactionAlreadyFound("Transaction already saved!");
        transactions.add(transaction);
        return Optional.of(transaction);
    }

}
