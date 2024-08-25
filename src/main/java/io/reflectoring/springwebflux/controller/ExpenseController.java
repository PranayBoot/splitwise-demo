package io.reflectoring.springwebflux.controller;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.Transaction;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.request.ExpenseRequest;
import io.reflectoring.springwebflux.service.BalanceService;
import io.reflectoring.springwebflux.service.ExpenseService;
import io.reflectoring.springwebflux.service.TransactionService;
import io.reflectoring.springwebflux.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserService userService; // Assuming you have a UserService for fetching users

    @PostMapping
    public ResponseEntity<Expense> addExpense(@RequestBody ExpenseRequest expenseRequest) {
        expenseRequest.setId(UUID.randomUUID().getLeastSignificantBits());
        Expense expense = expenseService.addExpense(expenseRequest);
        return new ResponseEntity<>(expense, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Expense>> getExpensesByUser(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        List<Expense> expenses = expenseService.getExpensesByUser(user);
        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }
}
