package io.reflectoring.springwebflux.controller;
import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.Transaction;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.repository.ExpenseRepository;
import io.reflectoring.springwebflux.service.TransactionService;
import io.reflectoring.springwebflux.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        List<Transaction> transactions = transactionService.getTransactionsByUser(user);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<List<Transaction>> getTransactionsByExpense(@PathVariable String expenseId) {
        Optional<Expense> expense = expenseRepository.findById(expenseId); // Assume you can fetch Expense by Id or handle this appropriately

         if(expense.isPresent()) {
             List<Transaction> transactions = transactionService.getTransactionsByExpense(expense.get());
             return new ResponseEntity<>(transactions, HttpStatus.OK);
         }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/{userId}/expense/{expenseId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUserAndExpense(@PathVariable String userId, @PathVariable String expenseId) {
        User user = userService.getUserById(userId);
        Expense expense = new Expense(); // Assume you can fetch Expense by Id or handle this appropriately
        expense.setId(expenseId);
        List<Transaction> transactions = transactionService.getTransactionsByUserAndExpense(user, expense);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String transactionId) {
        transactionService.deleteTransaction(transactionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
