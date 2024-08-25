package io.reflectoring.springwebflux.repository;

import io.reflectoring.springwebflux.model.Balance;
import io.reflectoring.springwebflux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends MongoRepository<Balance,String> {
    List<Balance> findByUser(User user);

    List<Balance> findByOtherUser(User otherUser);

    Optional<Balance> findByUserAndOtherUser(User user, User otherUser);
}
