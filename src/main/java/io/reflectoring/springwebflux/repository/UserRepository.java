package io.reflectoring.springwebflux.repository;

import io.reflectoring.springwebflux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
  Optional<User> findByEmail(String email);

  Optional<User> findByMobileNumber(String mobileNumber);

  User findUserById(String id);
}
