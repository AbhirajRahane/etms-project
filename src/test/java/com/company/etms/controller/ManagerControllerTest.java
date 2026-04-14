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
 * Test class for ManagerController.
 *
 * This class tests:
 * - Dashboard loading
 * - Task creation page
 * - Saving tasks
 * - Deleting tasks
 * - Updating tasks
 * - Task statistics APIs
 *
 * Note:
 * Security is disabled for testing.
 */
@WebMvcTest(ManagerController.class)
@AutoConfigureMockMvc(addFilters = false)
class ManagerControllerTest {

    // Used to simulate HTTP requests
    @Autowired
    private MockMvc mockMvc;

    // Mock services (no real DB calls)
    @MockBean
    private UserService userService;

    @MockBean
    private TaskService taskService;

    // ================== DASHBOARD ==================

    /**
     * Test: Manager dashboard loads successfully
     * Expected: users and tasks are present in model
     */
    @Test
    void testManagerDashboard() throws Exception {

        Mockito.when(userService.getAllUsers()).thenReturn(List.of());
        Mockito.when(taskService.getAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/manager/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("tasks"));
    }

    // ================== CREATE TASK PAGE ==================

    /**
     * Test: Create task page loads
     * Expected: empty task object + users list
     */
    @Test
    void testCreateTaskPage() throws Exception {

        Mockito.when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/manager/create-task"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("users"));
    }

    // ================== SAVE TASK SUCCESS ==================

    /**
     * Test: Save task successfully
     * Expected: success = true
     */
    @Test
    void testSaveTask_success() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        String json = """
                {
                  "title": "Test Task",
                  "description": "Test Desc",
                  "status": "Pending",
                  "userId": "1"
                }
                """;

        mockMvc.perform(post("/manager/save-task")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ================== SAVE TASK USER NOT FOUND ==================

    /**
     * Test: Save task when user not found
     * Expected: HTTP 404 and success = false
     */
    @Test
    void testSaveTask_userNotFound() throws Exception {

        Mockito.when(userService.getUserById(1L)).thenReturn(null);

        String json = """
                {
                  "title": "Test Task",
                  "description": "Test Desc",
                  "status": "Pending",
                  "userId": "1"
                }
                """;

        mockMvc.perform(post("/manager/save-task")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================== DELETE TASK ==================

    /**
     * Test: Delete task
     * Expected: success = true
     */
    @Test
    void testDeleteTask() throws Exception {

        mockMvc.perform(delete("/manager/delete-task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ================== UPDATE TASK SUCCESS ==================

    /**
     * Test: Update task inline successfully
     * Expected: success = true
     */
    @Test
    void testUpdateTaskInline_success() throws Exception {

        Task task = new Task();
        task.setId(1L);

        Mockito.when(taskService.getTaskById(1L)).thenReturn(task);

        String json = """
                {
                  "title": "Updated Task",
                  "status": "Completed"
                }
                """;

        mockMvc.perform(post("/manager/tasks/1/update-inline")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ================== UPDATE TASK NOT FOUND ==================

    /**
     * Test: Update task when task not found
     * Expected: HTTP 404 and success = false
     */
    @Test
    void testUpdateTaskInline_notFound() throws Exception {

        Mockito.when(taskService.getTaskById(1L)).thenReturn(null);

        mockMvc.perform(post("/manager/tasks/1/update-inline")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================== TASK COUNTS ==================

    /**
     * Test: Task count API
     * Expected:
     * total = 2
     * completed = 1
     * pending = 1
     */
    @Test
    void testTaskCounts() throws Exception {

        Task t1 = new Task();
        t1.setStatus("Completed");

        Task t2 = new Task();
        t2.setStatus("Pending");

        Mockito.when(taskService.getAllTasks()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/manager/task-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.pending").value(1));
    }

    // ================== TASK STATUSES ==================

    /**
     * Test: Get all task statuses
     * Expected: list with task details
     */
    @Test
    void testGetAllTaskStatuses() throws Exception {

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test");
        task.setStatus("Pending");

        User user = new User();
        user.setEmail("test@mail.com");
        task.setAssignedTo(user);

        Mockito.when(taskService.getAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/manager/tasks/statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test"))
                .andExpect(jsonPath("$[0].status").value("Pending"));
    }
}