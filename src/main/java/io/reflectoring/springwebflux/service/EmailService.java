package io.reflectoring.springwebflux.service;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.repository.BalanceRepository;
import io.reflectoring.springwebflux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;  // Spring's email sending component

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendAsyncEmails(Expense expense) {
        List<User> participants = expense.getParticipants();
        String payerEmail = expense.getPaidBy().getEmail();
        String subject = "New Expense Added";

        for (User participant : participants) {
            String recipientEmail = participant.getEmail();
            double amountOwed = calculateAmountOwed(expense, participant);
            String text = String.format("You have been added to a new expense. Amount owed to %s: %.2f", payerEmail, amountOwed);

            sendEmail(recipientEmail, subject, text);
        }
    }

    @Scheduled(cron = "0 0 12 * * MON")
    public void sendWeeklyEmailReport() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<Balance> balances = balanceRepository.findByUser(user);
            String subject = "Weekly Expense Report";
            StringBuilder text = new StringBuilder("Here is your weekly expense report:\n");

            for (Balance balance : balances) {
                String otherUserName = balance.getOtherUser().getName();
                double amount = balance.getAmount();
                text.append(String.format("%s owes you: %.2f\n", otherUserName, amount));
            }

            sendEmail(user.getEmail(), subject, text.toString());
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private double calculateAmountOwed(Expense expense, User participant) {
        // Logic to calculate the amount owed by the participant
        return balanceRepository.findByUserAndOtherUser(participant, expense.getPaidBy())
                .map(Balance::getAmount)
                .orElse(0.0);
    }
}
