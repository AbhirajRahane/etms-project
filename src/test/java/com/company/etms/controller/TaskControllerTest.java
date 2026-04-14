package com.company.etms.controller;

import com.company.etms.entity.Task;
import com.company.etms.entity.User;
import com.company.etms.service.TaskService;
import com.company.etms.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for TaskController (Employee side).
 *
 * This class tests:
 * - Viewing assigned tasks
 * - Updating task status
 *
 * Note:
 * Security is disabled to avoid authentication issues in tests.
 */
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    // Used to simulate HTTP requests
    @Autowired
    private MockMvc mockMvc;

    // Mock services (no real DB interaction)
    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    // ================== VIEW TASKS ==================

    /**
     * Test: View tasks page for employee
     * Expected:
     * - Page loads successfully
     * - Tasks are added to model
     */
    @Test
    void testViewTasks() throws Exception {

        // Create mock user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        // Mock user fetch
        Mockito.when(userService.getUserByEmail(Mockito.anyString()))
                .thenReturn(user);

        // Mock task list
        Mockito.when(taskService.getTasksByUser(user))
                .thenReturn(List.of());

        mockMvc.perform(get("/employee/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/tasks"))
                .andExpect(model().attributeExists("tasks"));
    }

    // ================== UPDATE STATUS SUCCESS ==================

    /**
     * Test: Update task status successfully
     * Expected:
     * - Task belongs to user
     * - Status updated
     * - success = true
     */
    @Test
    void testUpdateTaskStatus_success() throws Exception {

        // Mock user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        // Mock task assigned to user
        Task task = new Task();
        task.setId(1L);
        task.setAssignedTo(user);

        // Mock service responses
        Mockito.when(userService.getUserByEmail(Mockito.anyString()))
                .thenReturn(user);

        Mockito.when(taskService.getTaskById(1L))
                .thenReturn(task);

        mockMvc.perform(post("/employee/tasks/1/update-status")
                        .param("status", "Completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}