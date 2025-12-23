package com.example.AzureTestProject.Api.Controller;


import com.example.AzureTestProject.Api.Entity.Transaction;
import com.example.AzureTestProject.Api.Service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/txn")
public class Controller {


    @Autowired
    private TransactionService transactionService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/{arg}")
    public ResponseEntity<String> testArg(@PathVariable @Positive int arg) {
        return ResponseEntity.ok("Arg : " + arg);
    }

    @PostMapping("/add")
    public ResponseEntity<Transaction> add(@RequestBody @Valid Transaction transaction) {
        return transactionService.save(transaction).map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }


}
