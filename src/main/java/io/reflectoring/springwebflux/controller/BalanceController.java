package io.reflectoring.springwebflux.controller;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.service.BalanceService;
import io.reflectoring.springwebflux.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Balance>> getBalancesForUser(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        List<Balance> balances = balanceService.getBalancesForUser(user);
        return new ResponseEntity<>(balances, HttpStatus.OK);
    }

    @GetMapping("/other-user/{userId}")
    public ResponseEntity<List<Balance>> getBalancesForOtherUser(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        List<Balance> balances = balanceService.getBalancesForOtherUser(user);
        return new ResponseEntity<>(balances, HttpStatus.OK);
    }

    @GetMapping("/between/{user1Id}/{user2Id}")
    public ResponseEntity<Balance> getBalanceBetweenUsers(@PathVariable String user1Id, @PathVariable String user2Id) {
        User user1 = userService.getUserById(user1Id);
        User user2 = userService.getUserById(user2Id);
        Optional<Balance> balance = balanceService.getBalanceBetweenUsers(user1, user2);
        return balance.map(b -> new ResponseEntity<>(b, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Balance> createOrUpdateBalance(@RequestBody Balance balance) {
        Balance updatedBalance = balanceService.createOrUpdateBalance(balance);
        return new ResponseEntity<>(updatedBalance, HttpStatus.OK);
    }

    @DeleteMapping("/{balanceId}")
    public ResponseEntity<Void> deleteBalance(@PathVariable String balanceId) {
        balanceService.deleteBalance(balanceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
