package com.company.etms.controller;

import com.company.etms.entity.User;
import com.company.etms.service.TaskService;
import com.company.etms.service.UserService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling employee dashboard.
 * It manages:
 * - Displaying dashboard with task details
 * - Providing task counts (used for AJAX / API calls)
 */
@Controller
public class DashboardController {

    // Logger to track dashboard activity
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    // Services for business logic
    private final TaskService taskService;
    private final UserService userService;

    /**
     * Constructor-based dependency injection
     *
     * @param taskService Service for task operations
     * @param userService Service for user operations
     */
    public DashboardController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Displays the employee dashboard.
     *
     * Steps:
     * 1. Get logged-in user
     * 2. Fetch task statistics (total, completed, pending)
     * 3. Send data to dashboard view
     *
     * @param model Spring model to pass data to view
     * @param auth  Authentication object (current logged-in user)
     * @return dashboard page
     */
    @GetMapping("/dashboard")
    public String viewDashboard(Model model, Authentication auth) {

        // Log dashboard access
        logger.info("Dashboard accessed by user: {}", auth.getName());

        // Get logged-in user from database
        User user = userService.getUserByEmail(auth.getName());

        // Safety check: user must exist
        if (user == null) {
            logger.error("Logged-in user not found in database: {}", auth.getName());
            throw new RuntimeException("Logged-in user not found in database!");
        }

        // ---------------- TASK COUNT LOGIC ----------------
        // Count total, completed, and pending tasks
        int totalTasks = taskService.countTasksByUser(user);
        int completedTasks = taskService.countTasksByUserAndStatus(user, "Completed");
        int pendingTasks = taskService.countTasksByUserAndStatus(user, "Pending");

        // Log task statistics
        logger.info("Task stats for user {} -> Total: {}, Completed: {}, Pending: {}",
                user.getId(), totalTasks, completedTasks, pendingTasks);

        // ---------------- SEND DATA TO VIEW ----------------
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("pendingTasks", pendingTasks);

        // Send task list
        model.addAttribute("tasks", taskService.getTasksByUser(user));

        return "dashboard";
    }

    /**
     * Provides task counts as JSON response.
     * This is typically used for AJAX calls to update dashboard dynamically.
     *
     * @param userDetails current logged-in user details
     * @return map containing task counts (total, completed, pending)
     */
    @GetMapping("/employee/task-counts")
    @ResponseBody
    public Map<String, Integer> getTaskCounts(@AuthenticationPrincipal UserDetails userDetails) {

        // Log API access
        logger.info("Fetching task counts for user: {}", userDetails.getUsername());

        // Get user from database
        User user = userService.getUserByEmail(userDetails.getUsername());

        // Check if user exists
        if (user == null) {
            logger.warn("User not found while fetching task counts: {}", userDetails.getUsername());
        }

        // ---------------- PREPARE RESPONSE ----------------
        Map<String, Integer> counts = new HashMap<>();

        counts.put("total", taskService.countTasksByUser(user));
        counts.put("completed", taskService.countTasksByUserAndStatus(user, "Completed"));
        counts.put("pending", taskService.countTasksByUserAndStatus(user, "Pending"));

        // Log response data
        logger.info("Task counts sent for user {} -> {}", userDetails.getUsername(), counts);

        return counts;
    }
}