package com.company.etms.controller;

import com.company.etms.entity.Task;
import com.company.etms.entity.User;
import com.company.etms.service.TaskService;
import com.company.etms.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for manager operations.
 * Manager can:
 * - View dashboard
 * - Create tasks
 * - Update tasks
 * - Delete tasks
 * - View task statistics
 */
@Controller
@RequestMapping("/manager") // Base URL for all manager endpoints
public class ManagerController {

    // Logger for tracking manager actions
    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    // Services for business logic
    private final UserService userService;
    private final TaskService taskService;

    /**
     * Constructor-based dependency injection
     */
    public ManagerController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    // ---------------- DASHBOARD ----------------

    /**
     * Displays manager dashboard.
     * Shows all users and all tasks.
     *
     * @param model Spring model
     * @return manager dashboard page
     */
    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {

        logger.info("Manager dashboard accessed");

        // Fetch all users and tasks
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("tasks", taskService.getAllTasks());

        return "manager-dashboard";
    }

    // ---------------- CREATE TASK PAGE ----------------

    /**
     * Opens the create task page.
     *
     * @param model Spring model
     * @return create-task page
     */
    @GetMapping("/create-task")
    public String createTaskPage(Model model) {

        logger.info("Manager opened create task page");

        // Empty task object for form binding
        model.addAttribute("task", new Task());

        // Send user list for task assignment
        model.addAttribute("users", userService.getAllUsers());

        return "create-task";
    }

    // ---------------- CREATE TASK (AJAX) ----------------

    /**
     * Creates a new task using AJAX request.
     *
     * Steps:
     * 1. Read data from request
     * 2. Validate user
     * 3. Create and save task
     * 4. Return response to UI
     *
     * @param payload request data (JSON)
     * @return success/failure response
     */
    @PostMapping("/save-task")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveTaskAjax(@RequestBody Map<String, String> payload) {

        try {
            logger.info("Manager attempting to create task with payload: {}", payload);

            // ---------------- READ INPUT ----------------
            String title = payload.get("title");
            String description = payload.get("description");
            String status = payload.get("status");
            Long userId = Long.parseLong(payload.get("userId"));

            // ---------------- VALIDATE USER ----------------
            User user = userService.getUserById(userId);

            if (user == null) {
                logger.warn("Task creation failed - user not found: {}", userId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found"));
            }

            // ---------------- CREATE TASK ----------------
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(status);
            task.setAssignedTo(user);

            taskService.saveTask(task);

            logger.info("Task created successfully with ID: {} assigned to user: {}", task.getId(), userId);

            // ---------------- PREPARE RESPONSE ----------------
            Map<String, Object> taskData = Map.of(
                    "id", task.getId(),
                    "title", task.getTitle(),
                    "description", task.getDescription(),
                    "status", task.getStatus(),
                    "assignedTo", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail()
                    )
            );

            return ResponseEntity.ok(Map.of("success", true, "task", taskData));

        } catch (Exception e) {
            logger.error("Error while creating task", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ---------------- DELETE TASK ----------------

    /**
     * Deletes a task by ID.
     *
     * @param id task ID
     * @return success/failure response
     */
    @DeleteMapping("/delete-task/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTaskAjax(@PathVariable Long id) {

        try {
            logger.warn("Manager deleting task ID: {}", id);

            taskService.deleteTask(id);

            logger.info("Task deleted successfully: {}", id);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            logger.error("Error while deleting task ID: {}", id, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ---------------- UPDATE TASK ----------------

    /**
     * Updates task details inline (without page reload).
     *
     * @param taskId task ID
     * @param updates fields to update
     * @return success/failure response
     */
    @PostMapping("/tasks/{taskId}/update-inline")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTaskInline(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> updates) {

        logger.info("Manager updating task ID: {} with updates: {}", taskId, updates);

        // Fetch task
        Task task = taskService.getTaskById(taskId);

        if (task == null) {
            logger.warn("Task update failed - task not found: {}", taskId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Task not found"));
        }

        try {
            // ---------------- UPDATE FIELDS ----------------
            if (updates.containsKey("title")) task.setTitle(updates.get("title"));
            if (updates.containsKey("status")) task.setStatus(updates.get("status"));

            // Update assigned user if provided
            if (updates.containsKey("assignedToId")) {
                Long userId = Long.parseLong(updates.get("assignedToId"));
                User user = userService.getUserById(userId);

                if (user != null) {
                    task.setAssignedTo(user);
                } else {
                    logger.warn("User not found: {}", userId);

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", "User not found"));
                }
            }

            // Save updated task
            taskService.saveTask(task);

            logger.info("Task updated successfully: {}", taskId);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            logger.error("Error while updating task ID: {}", taskId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ---------------- TASK COUNTS ----------------

    /**
     * Returns total, completed, and pending task counts.
     * Used for dashboard statistics.
     *
     * @return task count data
     */
    @GetMapping("/task-counts")
    @ResponseBody
    public Map<String, Object> getTaskCounts() {

        logger.info("Fetching manager task counts");

        long total = taskService.getAllTasks().size();
        long completed = taskService.getAllTasks().stream()
                .filter(t -> "Completed".equals(t.getStatus())).count();
        long pending = taskService.getAllTasks().stream()
                .filter(t -> "Pending".equals(t.getStatus())).count();

        return Map.of("total", total, "completed", completed, "pending", pending);
    }

    // ---------------- TASK STATUS LIST ----------------

    /**
     * Returns all task statuses.
     * Used for refreshing dashboard dynamically.
     *
     * @return list of task status data
     */
    @GetMapping("/tasks/statuses")
    @ResponseBody
    public List<Map<String, Object>> getAllTasksStatuses() {

        logger.info("Fetching all task statuses");

        List<Task> tasks = taskService.getAllTasks();
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        // Convert task objects to simple map format
        for (Task task : tasks) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("status", task.getStatus());
            map.put("assignedTo", task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : "");
            result.add(map);
        }

        return result;
    }
}