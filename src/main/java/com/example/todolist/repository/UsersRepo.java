package com.example.todolist.repository;

import com.example.todolist.entity.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends CrudRepository<Users, Long> {
//    Users findById(String username);

    Optional<Users> findByEmailAndPassword(String email, String password);

    Optional<Users> findByEmail(String username);
}
