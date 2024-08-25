package io.reflectoring.springwebflux.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.reflectoring.springwebflux.request.ExpenseRequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@Document("expenses")
public class Expense {
    @Id
    private String id;
    private double totalAmount;

    @Indexed
    @Field(targetType = FieldType.STRING)
    private ExpenseType expenseType; // EQUAL, EXACT, PERCENT

    @DBRef
    private User paidBy;
    @DBRef
    private List<User> participants;
    @DBRef
    private List<Balance> balances=new ArrayList<>();
    @DBRef
    private List<Transaction> transactions;
   @DBRef
    private ExpenseRequest expenseRequest;


    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", totalAmount=" + totalAmount +
                ", expenseType=" + expenseType +
                ", paidBy=" + paidBy +
                ", participants=" + participants +
                ", balances=" + balances +
                ", transactions=" + transactions +
                ", expenseRequest=" + expenseRequest +
                '}';
    }
}
