package com.example.todolist.repository;

import com.example.todolist.entity.UserTask;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskRepo extends CrudRepository<UserTask, Long> {
    Optional<UserTask> findById(Long id);

    List<UserTask> findByUsersId(Long id);

    List<UserTask> findByUsersIdAndStatus(Long id, String status);

    List<UserTask> findByStatus(String status);
}
