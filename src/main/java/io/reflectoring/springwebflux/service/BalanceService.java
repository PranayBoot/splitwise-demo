package io.reflectoring.springwebflux.service;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.repository.BalanceRepository;
import io.reflectoring.springwebflux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BalanceService {

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Balance> getBalancesForUser(User user) {
        return balanceRepository.findByUser(user);
    }

    public List<Balance> getBalancesForOtherUser(User user) {
        return balanceRepository.findByOtherUser(user);
    }

    public Optional<Balance> getBalanceBetweenUsers(User user1, User user2) {
        return balanceRepository.findByUserAndOtherUser(user1, user2);
    }

    @Transactional
    public Balance createOrUpdateBalance(Balance balance) {

        if(balance.getUser()!=null){
             if(balance.getUser().getPaidBy()!=null){
            User padUser=       userRepository.findUserById(balance.getUser().getPaidBy().getId());
                    balance.getExpense().setPaidBy(padUser);
             }
            userRepository.save(balance.getUser());
        }
        return balanceRepository.save(balance);
    }

    public void deleteBalance(String balanceId) {
        balanceRepository.deleteById(balanceId);
    }
}
