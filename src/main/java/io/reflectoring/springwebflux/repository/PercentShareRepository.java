package io.reflectoring.springwebflux.repository;

import io.reflectoring.springwebflux.model.Expense;
import io.reflectoring.springwebflux.model.PercentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PercentShareRepository extends MongoRepository<PercentShare,String> {

    List<PercentShare> findByExpense(Expense expense);
}
