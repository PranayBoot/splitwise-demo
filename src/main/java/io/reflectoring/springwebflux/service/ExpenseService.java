package io.reflectoring.springwebflux.service;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.Transaction;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.repository.BalanceRepository;
import io.reflectoring.springwebflux.repository.ExpenseRepository;
import io.reflectoring.springwebflux.repository.TransactionRepository;
import io.reflectoring.springwebflux.repository.UserRepository;
import io.reflectoring.springwebflux.request.ExpenseRequest;
import org.hibernate.engine.internal.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ExpenseService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;
    @Transactional
    public User getUserWithParticipants(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Access participants here
        return user;
    }

    @Transactional
    public Expense addExpense(ExpenseRequest expenseRequest) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID().toString());
        expense.setTotalAmount(expenseRequest.getTotalAmount());
        expense.setExpenseType(expenseRequest.getExpenseType());

        User paidBy = userRepository.findById(expenseRequest.getPaidById()).orElseThrow(() -> new RuntimeException("User not found"));
        expense.setPaidBy(paidBy);

        Iterable<User>userIterator=  userRepository.findAllById(expenseRequest.getParticipantIds());
        List<User> participants = StreamSupport.stream(userIterator.spliterator(),false).collect(Collectors.toList());
         List<User>participantsList=new ArrayList<>();
        expense.setParticipants(participants);

        calculateShares(expense);


        expenseRepository.save(expense);
        emailService.sendAsyncEmails(expense);
        updateBalances(expense);
        return expense;
    }

    private void saveBalance(Expense expense){
        if(expense.getBalances()!=null && expense.getBalances().size()>0) {
            for (Balance b : expense.getBalances()) {
                Balance balance = new Balance();
                balance.setAmount(b.getAmount());
                balance.setExpense(b.getExpense());
                balance.setUser(b.getUser());
                balance.setOtherUser(b.getOtherUser());
                balanceRepository.save(b);
            }
        }
    }
    private void calculateShares(Expense expense) {
        double totalAmount = expense.getTotalAmount();
        List<User> participants = expense.getParticipants();

        switch (expense.getExpenseType()) {
            case EQUAL:
                double equalShare = totalAmount / participants.size();
                for (User participant : participants) {
                    createOrUpdateBalance(expense, participant, equalShare);
                }
                break;

            case EXACT:
                // Assuming that exact shares are provided in a map or list alongside the participants
                Map<User, Double> exactShares = getExactShares(expense);
                double sumExactShares = exactShares.values().stream().mapToDouble(Double::doubleValue).sum();

                if (Math.abs(sumExactShares - totalAmount) > 0.01) { // Allowing for floating-point precision issues
                    throw new IllegalArgumentException("Exact shares do not sum up to the total amount.");
                }

                for (Map.Entry<User, Double> entry : exactShares.entrySet()) {
                    User participant = entry.getKey();
                    double amountOwed = entry.getValue();
                    createOrUpdateBalance(expense, participant, amountOwed);
                }
                break;

            case PERCENT:
                double totalPercent = 0.0;
                Map<User, Double> percentShares = getPercentShares(expense);

                for (Double percent : percentShares.values()) {
                    totalPercent += percent;
                }

                if (Math.abs(totalPercent - 100.0) > 0.01) { // Allowing for floating-point precision issues
                    throw new IllegalArgumentException("Percent shares do not sum up to 100.");
                }

                for (Map.Entry<User, Double> entry : percentShares.entrySet()) {
                    User participant = entry.getKey();
                    double percentage = entry.getValue();
                    double amountOwed = totalAmount * (percentage / 100);
                    createOrUpdateBalance(expense, participant, amountOwed);
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported expense type.");
        }
    }

    private void createOrUpdateBalance(Expense expense, User participant, double amountOwed) {
        User payer = expense.getPaidBy();
         saveBalance(expense);
        // Fetch existing balance between payer and participant
        Optional<Balance> existingBalanceOpt = balanceRepository.findByUserAndOtherUser(participant, payer);

        if (existingBalanceOpt.isPresent()) {
            // If an existing balance is found, update the amount
            Balance existingBalance = existingBalanceOpt.get();
            double newAmount = existingBalance.getAmount() + amountOwed;
            existingBalance.setAmount(newAmount);
            balanceRepository.save(existingBalance);
        } else {
            // If no existing balance is found, create a new balance entry
            Balance newBalance = new Balance();
            newBalance.setUser(participant);
            newBalance.setOtherUser(payer);
            newBalance.setExpense(expense);
            newBalance.setAmount(amountOwed);
    //        entityManager.merge(newBalance);
        balanceRepository.save(newBalance);
        }
    }
    private Map<User, Double> getExactShares(Expense expense) {
        // Logic to retrieve exact shares for each participant
        // This could be from the expense request or a database
        return new HashMap<>(); // Replace with actual implementation
    }

    private Map<User, Double> getPercentShares(Expense expense) {
        // Logic to retrieve percent shares for each participant
        // This could be from the expense request or a database
        return new HashMap<>(); // Replace with actual implementation
    }
    private void updateBalances(Expense expense) {
        List<User> participants = expense.getParticipants();

        // Clear existing balances related to this expense
        for (User participant : participants) {
            balanceRepository.findByUserAndOtherUser(participant, expense.getPaidBy()).ifPresent(balance -> {
                balanceRepository.deleteById(balance.getId());
            });
        }

        // Update balances for each participant
        for (User participant : participants) {
            double amountOwed = calculateAmountOwed(expense, participant);
            if (amountOwed != 0) {
                Balance balance = new Balance();
                balance.setUser(participant);
                balance.setOtherUser(expense.getPaidBy());
                balance.setExpense(expense);
                balance.setAmount(amountOwed);
                balanceRepository.save(balance);
            }
        }
    }
    private double calculateAmountOwed(Expense expense, User participant) {
        // Logic to calculate the amount a participant owes
        // This should reflect the shares calculated in calculateShares method
        return balanceRepository.findByUserAndOtherUser(participant, expense.getPaidBy())
                .map(Balance::getAmount)
                .orElse(0.0);
    }
    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByPaidBy(user);
    }
}
