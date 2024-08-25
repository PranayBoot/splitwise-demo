package io.reflectoring.springwebflux.controller;

import io.reflectoring.springwebflux.model.User;
import io.reflectoring.springwebflux.request.UserDto;
import io.reflectoring.springwebflux.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto userRequest) {
        try {
            User save=new User();
            save.setPaidBy(userRequest.getPaidBy());
            save.setEmail(userRequest.getEmail());
            save.setMobileNumber(userRequest.getMobileNumber());
            save.setName(userRequest.getName());
            save.setParticipants(userRequest.getParticipants());
            save.setTotalAmount(userRequest.getTotalAmount());
            save.setBalances(userRequest.getBalances());
            save.setTransactions(userRequest.getTransactions());
            User user = userService.createUser(save);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            e.printStackTrace();
            // Log the error and return a proper response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
