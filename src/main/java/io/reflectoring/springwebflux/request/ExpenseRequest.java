package io.reflectoring.springwebflux.request;

import io.reflectoring.springwebflux.model.ExpenseType;
import io.reflectoring.springwebflux.model.PercentShare;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ExpenseRequest {

    @Id
    private Long id;
    private double totalAmount; // The total amount of the expense
    @Indexed
    @Field(targetType = FieldType.STRING)
    private ExpenseType expenseType; // The type of expense: EQUAL, EXACT, or PERCENT
    private String paidById; // ID of the user who paid for the expense
    private List<String> participantIds = new ArrayList<>();
    private Map<Long, Double> percentShares = new HashMap<>();
}
