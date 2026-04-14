package com.company.etms.repository;

import com.company.etms.entity.Task;
import com.company.etms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Task entity.
 * Provides database operations for Task objects, including
 * basic CRUD methods inherited from JpaRepository.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {  // <- changed Integer to Long

    /**
     * Finds all tasks assigned to a specific user.
     *
     * @param user The user to whom tasks are assigned
     * @return List of tasks assigned to the given user
     */
    List<Task> findByAssignedTo(User user);
}