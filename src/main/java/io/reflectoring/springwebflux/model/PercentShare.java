package io.reflectoring.springwebflux.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class PercentShare {

   @Id
    private String id;
    @DBRef
    private Expense expense;
    @DBRef
    private User user;
    private double percentage;

}
