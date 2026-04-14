package com.company.etms.service;

import com.company.etms.entity.Task;
import com.company.etms.entity.User;
import com.company.etms.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class that handles all task-related business logic.
 *
 * This includes:
 * - Creating tasks
 * - Updating tasks
 * - Deleting tasks
 * - Fetching tasks
 */
@Service
public class TaskService {

    // Logger for tracking actions and errors
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    // Repository to interact with database
    private final TaskRepository repo;

    // Constructor injection
    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    /**
     * Save a new task or update an existing task.
     *
     * Steps:
     * 1. Set updated time
     * 2. Save task in DB
     * 3. Log success or error
     *
     * @param task task object to save
     */
    public void saveTask(Task task) {
        try {
            logger.info("Saving task: {}", task.getTitle());

            // Set last updated time
            task.setUpdatedAt(LocalDateTime.now());

            // Save task
            repo.save(task);

            logger.info("Task saved successfully with ID: {}", task.getId());

        } catch (Exception e) {
            logger.error("Error while saving task", e);
            throw e;
        }
    }

    /**
     * Get all tasks assigned to a specific user.
     *
     * @param user employee
     * @return list of tasks (empty if user is null)
     */
    public List<Task> getTasksByUser(User user) {

        if (user == null) {
            logger.warn("getTasksByUser called with null user");
            return List.of();
        }

        logger.info("Fetching tasks for user ID: {}", user.getId());

        return repo.findByAssignedTo(user);
    }

    /**
     * Get all tasks in the system (used by manager).
     *
     * @return list of all tasks
     */
    public List<Task> getAllTasks() {
        logger.info("Fetching all tasks");
        return repo.findAll();
    }

    /**
     * Get a task using its ID.
     *
     * @param id task ID
     * @return task object or null if not found
     */
    public Task getTaskById(Long id) {

        logger.info("Fetching task by ID: {}", id);

        Task task = repo.findById(id).orElse(null);

        if (task == null) {
            logger.warn("Task not found for ID: {}", id);
        }

        return task;
    }

    /**
     * Delete a task using its ID.
     *
     * @param id task ID
     */
    public void deleteTask(Long id) {
        try {
            logger.warn("Deleting task with ID: {}", id);

            repo.deleteById(id);

            logger.info("Task deleted successfully: {}", id);

        } catch (Exception e) {
            logger.error("Error while deleting task ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Update task status (only allowed for assigned employee).
     *
     * Steps:
     * 1. Check employee exists
     * 2. Verify task belongs to employee
     * 3. Update status and time
     * 4. Save task
     *
     * @param taskId   task ID
     * @param employee logged-in user
     * @param status   new status
     * @return true if updated, false otherwise
     */
    public boolean updateTaskStatus(Long taskId, User employee, String status) {

        if (employee == null) {
            logger.warn("updateTaskStatus called with null employee");
            return false;
        }

        logger.info("Updating task ID: {} by employee ID: {}", taskId, employee.getId());

        return repo.findById(taskId)

                // Check task belongs to this employee
                .filter(task -> task.getAssignedTo() != null
                        && Objects.equals(task.getAssignedTo().getId(), employee.getId()))

                // If valid → update
                .map(task -> {
                    task.setStatus(status);
                    task.setUpdatedAt(LocalDateTime.now());

                    repo.save(task);

                    logger.info("Task status updated successfully for task ID: {}", taskId);
                    return true;
                })

                // If invalid
                .orElseGet(() -> {
                    logger.warn("Task update failed. Not found or not assigned. Task ID: {}", taskId);
                    return false;
                });
    }

    /**
     * Count total tasks assigned to a user.
     *
     * @param user employee
     * @return total task count
     */
    public int countTasksByUser(User user) {

        if (user == null) {
            logger.warn("countTasksByUser called with null user");
            return 0;
        }

        int count = repo.findByAssignedTo(user).size();

        logger.info("Total tasks for user ID {}: {}", user.getId(), count);

        return count;
    }

    /**
     * Count tasks by status for a user.
     * Example: Completed, Pending
     *
     * @param user   employee
     * @param status task status
     * @return count of matching tasks
     */
    public int countTasksByUserAndStatus(User user, String status) {

        if (user == null || status == null) {
            logger.warn("countTasksByUserAndStatus called with null values");
            return 0;
        }

        int count = (int) repo.findByAssignedTo(user).stream()
                .filter(task -> status.equalsIgnoreCase(task.getStatus()))
                .count();

        logger.info("Tasks for user ID {} with status '{}': {}", user.getId(), status, count);

        return count;
    }
}