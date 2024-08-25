package io.reflectoring.springwebflux.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Balance {

    @Id
    private String id;
   @DBRef
    private User user;

   @DBRef
    private User otherUser;
   @DBRef
    private Expense expense;
    private double amount;


    @Override
    public String toString() {
        return "Balance{" +
                "id=" + id +
                ", user=" + user +
                ", otherUser=" + otherUser +
                ", expense=" + expense +
                ", amount=" + amount +
                '}';
    }
}
