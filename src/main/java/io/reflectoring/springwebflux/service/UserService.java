package io.reflectoring.springwebflux.service;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.repository.BalanceRepository;
import io.reflectoring.springwebflux.repository.ExpenseRepository;
import io.reflectoring.springwebflux.repository.PercentShareRepository;
import io.reflectoring.springwebflux.repository.UserRepository;
import io.reflectoring.springwebflux.request.ExpenseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PercentShareRepository percentShareRepository;

    @Transactional
    public User createUser(User userRequest) {
        String paidById =null;
        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setMobileNumber(userRequest.getMobileNumber());
         if(userRequest.getPaidBy()==null) {
//        user.setEmail(userRequest.getEmail());
//        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Hash the password
//        user.setFirstName(userRequest.getFirstName());
//        user.setLastName(userRequest.getLastName());
//        user.setDateOfBirth(userRequest.getDateOfBirth()); // Set if applicable
//        user.setPhoneNumber(userRequest.getPhoneNumber()); // Set if applicable
             User paidBy = userRepository.findById(paidById).orElse(null);
            user.setPaidBy(paidBy);
         }
         User userPadBy=userRequest.getPaidBy();
//         if(userPadBy.getId()==null){
//             userRepository.save(userPadBy);
//         }
        userRequest.setId(UUID.randomUUID().toString());
        return userRepository.save(userRequest);
    }

      @org.springframework.transaction.annotation.Transactional(readOnly = true)
     public  User getUserById(String id){
         return  userRepository.findUserById(id);
     }

    public Expense addExpense(ExpenseRequest expenseRequest) {
        Expense expense = new Expense();
        expense.setTotalAmount(expenseRequest.getTotalAmount());
        expense.setExpenseType(expenseRequest.getExpenseType());

        User paidBy = userRepository.findById(expenseRequest.getPaidById()).orElseThrow(() -> new RuntimeException("User not found"));
        expense.setPaidBy(paidBy);
        Criteria criteria=new Criteria();
        criteria.all(expenseRequest.getParticipantIds());
        Query query=new Query(criteria);
      Iterable<User>userIterator=  userRepository.findAllById(expenseRequest.getParticipantIds());
        List<User> participants =StreamSupport.stream(userIterator.spliterator(),false).collect(Collectors.toList());
        expense.setParticipants(participants);

        calculateShares(expense);
        updateBalances(expense);

        expenseRepository.save(expense);
        emailService.sendAsyncEmails(expense);

        return expense;
    }

    private Map<User, Double> getExactShares(Expense expense) {
        // Implement the logic to retrieve exact shares
        // This could be from the expense request or a database
        return new HashMap<>(); // Replace with actual implementation
    }

    private Map<User, Double> getPercentShares(Expense expense) {
        ExpenseRequest expenseRequest =expense.getExpenseRequest();// Retrieve the ExpenseRequest associated with this Expense

        List<String> participantIds = expenseRequest.getParticipantIds();
        Map<User, Double> userPercentShares = new HashMap<>();

        for (String userId : participantIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            // Assume you have a method to get the percentage share for each user
            Double percent = getPercentageShareForUser(userId,expenseRequest);
            userPercentShares.put(user, percent);
        }

        return userPercentShares;
    }

    private Double getPercentageShareForUser(String userId, ExpenseRequest expenseRequest) {
        // Retrieve the percentage share for the given user ID
        return expenseRequest.getPercentShares().get(userId);
    }
    private void calculateShares(Expense expense) {
        double totalAmount = expense.getTotalAmount();
        List<User> participants = expense.getParticipants();
        User payer = expense.getPaidBy();

        switch (expense.getExpenseType()) {
            case EQUAL:
                // Each participant pays an equal share
                double equalShare = totalAmount / participants.size();
                for (User participant : participants) {
                    // Skip the payer if the expense type is equal
                    if (!participant.equals(payer)) {
                        createOrUpdateBalance(expense, participant, equalShare);
                    }
                }
                break;

            case EXACT:
                // Exact shares should be provided in the request or calculated separately
                Map<User, Double> exactShares = getExactShares(expense);
                double sumExactShares = exactShares.values().stream().mapToDouble(Double::doubleValue).sum();

                if (Math.abs(sumExactShares - totalAmount) > 0.01) {
                    // Ensure the sum of exact shares matches the total amount
                    throw new IllegalArgumentException("Exact shares do not sum up to the total amount.");
                }

                for (Map.Entry<User, Double> entry : exactShares.entrySet()) {
                    User participant = entry.getKey();
                    double amountOwed = entry.getValue();
                    if (!participant.equals(payer)) {
                        createOrUpdateBalance(expense, participant, amountOwed);
                    }
                }
                break;

            case PERCENT:
                // Percent shares should be provided in the request or calculated separately
                Map<User, Double> percentShares = getPercentShares(expense);
                double totalPercent = percentShares.values().stream().mapToDouble(Double::doubleValue).sum();

                if (Math.abs(totalPercent - 100.0) > 0.01) {
                    // Ensure the total percent is 100
                    throw new IllegalArgumentException("Percent shares do not sum up to 100.");
                }

                for (Map.Entry<User, Double> entry : percentShares.entrySet()) {
                    User participant = entry.getKey();
                    double percentage = entry.getValue();
                    double amountOwed = totalAmount * (percentage / 100);
                    if (!participant.equals(payer)) {
                        createOrUpdateBalance(expense, participant, amountOwed);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported expense type.");
        }
    }
    private void createOrUpdateBalance(Expense expense, User participant, double amountOwed) {
        User payer = expense.getPaidBy();

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
            balanceRepository.save(newBalance);
        }
    }
    private void updateBalances(Expense expense) {
        User payer = expense.getPaidBy();
        List<User> participants = expense.getParticipants();

        // Remove existing balances related to this expense
        for (User participant : participants) {
            balanceRepository.findByUserAndOtherUser(participant, payer).ifPresent(balance -> {
                balanceRepository.deleteById(balance.getId());
            });
        }

        // Recalculate and update balances for each participant
        for (User participant : participants) {
            double amountOwed = calculateAmountOwed(expense, participant);
            if (amountOwed != 0) {
                Balance balance = new Balance();
                balance.setUser(participant);
                balance.setOtherUser(payer);
                balance.setExpense(expense);
                balance.setAmount(amountOwed);
                balanceRepository.save(balance);
            }
        }
    }

    private double calculateAmountOwed(Expense expense, User participant) {
        // The logic here should reflect the share calculation from calculateShares method
        return balanceRepository.findByUserAndOtherUser(participant, expense.getPaidBy())
                .map(Balance::getAmount)
                .orElse(0.0);
    }

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByPaidBy(user);
    }
}
