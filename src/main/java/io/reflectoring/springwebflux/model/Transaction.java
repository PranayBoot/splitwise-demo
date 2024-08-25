package io.reflectoring.springwebflux.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document("transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {
     @Id
    private String id;
    @DBRef
    private User user;
    @DBRef
    private Expense expense;
    private double amount;
    private LocalDateTime timestamp;
}
