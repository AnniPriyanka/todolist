package com.example.todolist.repository;

import com.example.todolist.entity.Tasks;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TasksRepo extends CrudRepository<Tasks, Long> {
    Optional<Tasks> findById(Long id);
    List<Tasks> findByCreatedBy(String createdBy);

}
