package io.reflectoring.springwebflux.service;

import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.Transaction;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUser(user);
    }

    public List<Transaction> getTransactionsByExpense(Expense expense) {
        return transactionRepository.findByExpense(expense);
    }

    public List<Transaction> getTransactionsByUserAndExpense(User user, Expense expense) {
        return transactionRepository.findByUserAndExpense(user, expense);
    }

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(String transactionId) {
        transactionRepository.deleteById(transactionId);
    }
}
