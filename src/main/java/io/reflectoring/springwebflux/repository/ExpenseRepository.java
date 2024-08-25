package io.reflectoring.springwebflux.repository;

import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends MongoRepository<Expense,String> {
    List<Expense> findByPaidBy(User user);
}
