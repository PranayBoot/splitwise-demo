package io.reflectoring.springwebflux.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Document("user")
@Accessors(chain = true)
public class User {

    public User() {
    }

   @Id
    private String id;

    private double totalAmount;
    private String email;
    private String mobileNumber;
    private String name;


    @Indexed
    @Field(targetType = FieldType.STRING)
    private ExpenseType expenseType; // EQUAL, EXACT, PERCENT

    @JsonBackReference
    private User paidBy;

    @DBRef
    @JsonManagedReference
    private List<User> participants=new ArrayList<>();
    @DBRef
    private List<Balance> balances=new ArrayList<>();
    @DBRef
   private List<Transaction> transactions=new ArrayList<>();

    public void setPaidBy(User paidBy) {
        this.paidBy = paidBy;
        if (paidBy != null) {
            paidBy.getParticipants().add(this);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", expenseType=" + expenseType +
                '}';
    }
}
