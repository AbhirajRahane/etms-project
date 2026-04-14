package com.company.etms.controller;

import com.company.etms.entity.Task;
import com.company.etms.entity.User;
import com.company.etms.service.TaskService;
import com.company.etms.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for employee task operations.
 * Employees can:
 * - View their assigned tasks
 * - Update task status
 */
@Controller
@RequestMapping("/employee") // Base URL for employee endpoints
public class TaskController {

    // Logger for tracking employee actions
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    // Services for business logic
    private final TaskService taskService;
    private final UserService userService;

    /**
     * Constructor-based dependency injection
     */
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Displays all tasks assigned to the logged-in employee.
     *
     * Steps:
     * 1. Get logged-in user
     * 2. Fetch assigned tasks
     * 3. Send tasks to view
     *
     * @param model        Spring model
     * @param userDetails  current logged-in user
     * @return employee tasks page
     */
    @GetMapping("/tasks")
    public String viewTasks(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        // Log access
        logger.info("Employee {} is viewing tasks", userDetails.getUsername());

        // Get employee from database
        User employee = userService.getUserByEmail(userDetails.getUsername());

        // Safety check
        if (employee == null) {
            logger.warn("Employee not found for username: {}", userDetails.getUsername());
            return "employee/tasks";
        }

        // Fetch tasks assigned to this employee
        List<Task> tasks = taskService.getTasksByUser(employee);

        // Log number of tasks
        logger.info("Total tasks fetched for employee {}: {}", employee.getEmail(), tasks.size());

        // Send data to view
        model.addAttribute("tasks", tasks);

        return "employee/tasks";
    }

    /**
     * Updates task status using AJAX request.
     *
     * Steps:
     * 1. Get status from request (form or JSON)
     * 2. Validate input
     * 3. Check task ownership (security check)
     * 4. Update and save task
     *
     * @param taskId       task ID
     * @param status       status from form (optional)
     * @param payload      status from JSON (optional)
     * @param userDetails  current logged-in user
     * @return JSON response (success or error)
     */
    @PostMapping("/tasks/{taskId}/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTaskStatusAjax(
            @PathVariable Long taskId,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Log update attempt
        logger.info("Employee {} attempting to update task ID: {}", 
                userDetails.getUsername(), taskId);

        // ---------------- GET STATUS ----------------
        // If status not from form, try JSON payload
        if (status == null && payload != null) {
            status = payload.get("status");
        }

        // Validate status
        if (status == null || status.isBlank()) {
            logger.warn("Task update failed - status is missing for task ID: {}", taskId);

            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Status is required"));
        }

        // ---------------- FETCH DATA ----------------
        User employee = userService.getUserByEmail(userDetails.getUsername());
        Task task = taskService.getTaskById(taskId);

        // Task not found
        if (task == null) {
            logger.warn("Task not found: {}", taskId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Task not found"));
        }

        // ---------------- SECURITY CHECK ----------------
        // Ensure employee can update only their own tasks
        if (task.getAssignedTo() == null || 
            !task.getAssignedTo().getId().equals(employee.getId())) {

            logger.warn("Unauthorized update attempt by {} on task ID: {}", 
                    userDetails.getUsername(), taskId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "You are not allowed to update this task"));
        }

        // ---------------- UPDATE TASK ----------------
        try {
            logger.info("Updating task ID {} to status '{}' by employee {}", 
                    taskId, status, userDetails.getUsername());

            // Set new status
            task.setStatus(status);

            // Save task
            taskService.saveTask(task);

            logger.info("Task ID {} updated successfully to '{}' by {}", 
                    taskId, status, userDetails.getUsername());

            return ResponseEntity.ok(Map.of("success", true, "status", status));

        } catch (Exception e) {
            logger.error("Error while updating task ID: {}", taskId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to update task"));
        }
    }
}