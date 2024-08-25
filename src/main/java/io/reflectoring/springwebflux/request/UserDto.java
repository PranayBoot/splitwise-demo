package io.reflectoring.springwebflux.request;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reflectoring.springwebflux.model.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Data
@Getter
@Setter
@ToString
public class UserDto {
    private Long id;
    @JsonProperty("totalAmount")
    private double totalAmount;
    @JsonProperty("email")
    private String email;
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    @JsonProperty("name")
    private String name;
    @JsonProperty("expenseType")
    private String expenseType; // EQUAL, EXACT, PERCENT
    @JsonProperty("paidBy")
    private User paidBy;
    @JsonProperty("participants")
    private List<User> participants=new ArrayList<>();
    @JsonProperty("balances")
    //@JsonManagedReference
    private List<Balance> balances=new ArrayList<>();
    @JsonProperty("transactions")
    private List<Transaction> transactions=new ArrayList<>();
}
