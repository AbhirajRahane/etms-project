package com.company.etms.service;

import com.company.etms.entity.Task;
import com.company.etms.entity.User;
import com.company.etms.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TaskService.
 *
 * This class verifies:
 * - Task creation and saving
 * - Fetching tasks (all / by id / by user)
 * - Updating task status
 * - Deleting tasks
 * - Counting tasks
 */
class TaskServiceTest {

    // Mock repository (no real DB calls)
    @Mock
    private TaskRepository repo;

    // Service with injected mock
    @InjectMocks
    private TaskService taskService;

    /**
     * Initialize mocks before each test
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ================== SAVE TASK ==================

    /**
     * Test: Save a task
     *
     * Expected:
     * - Task is saved in repository
     * - updatedAt field is set
     */
    @Test
    void testSaveTask() {

        Task task = new Task();
        task.setTitle("Test Task");

        // Call method
        taskService.saveTask(task);

        // Verify save() is called once
        verify(repo, times(1)).save(task);

        // Check timestamp is set
        assertNotNull(task.getUpdatedAt());
    }

    // ================== GET ALL TASKS ==================

    /**
     * Test: Fetch all tasks
     *
     * Expected:
     * - Correct list is returned
     */
    @Test
    void testGetAllTasks() {

        List<Task> tasks = List.of(new Task(), new Task());

        when(repo.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(repo, times(1)).findAll();
    }

    // ================== GET TASK BY ID ==================

    /**
     * Test: Task found by ID
     */
    @Test
    void testGetTaskById_found() {

        Task task = new Task();
        task.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test: Task not found by ID
     *
     * Expected:
     * - Null is returned
     */
    @Test
    void testGetTaskById_notFound() {

        when(repo.findById(1L)).thenReturn(Optional.empty());

        Task result = taskService.getTaskById(1L);

        assertNull(result);
    }

    // ================== DELETE TASK ==================

    /**
     * Test: Delete task by ID
     *
     * Expected:
     * - deleteById() is called
     */
    @Test
    void testDeleteTask() {

        Long id = 1L;

        taskService.deleteTask(id);

        verify(repo, times(1)).deleteById(id);
    }

    // ================== GET TASKS BY USER ==================

    /**
     * Test: Get tasks for valid user
     *
     * Expected:
     * - List of tasks is returned
     */
    @Test
    void testGetTasksByUser_validUser() {

        User user = new User();
        user.setId(1L);

        List<Task> tasks = List.of(new Task());

        when(repo.findByAssignedTo(user)).thenReturn(tasks);

        List<Task> result = taskService.getTasksByUser(user);

        assertEquals(1, result.size());
    }

    /**
     * Test: Get tasks with null user
     *
     * Expected:
     * - Empty list returned (no crash)
     */
    @Test
    void testGetTasksByUser_nullUser() {

        List<Task> result = taskService.getTasksByUser(null);

        assertTrue(result.isEmpty());
    }

    // ================== UPDATE TASK STATUS ==================

    /**
     * Test: Update task status successfully
     *
     * Expected:
     * - Status is updated
     * - Task is saved
     * - Method returns true
     */
    @Test
    void testUpdateTaskStatus_success() {

        User user = new User();
        user.setId(1L);

        Task task = new Task();
        task.setId(1L);
        task.setAssignedTo(user);

        when(repo.findById(1L)).thenReturn(Optional.of(task));

        boolean result = taskService.updateTaskStatus(1L, user, "Completed");

        assertTrue(result);
        assertEquals("Completed", task.getStatus());

        verify(repo).save(task);
    }

    /**
     * Test: Update fails when task belongs to another user
     *
     * Expected:
     * - Status is NOT updated
     * - Method returns false
     */
    @Test
    void testUpdateTaskStatus_fail_wrongUser() {

        User user = new User();
        user.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        Task task = new Task();
        task.setAssignedTo(otherUser);

        when(repo.findById(1L)).thenReturn(Optional.of(task));

        boolean result = taskService.updateTaskStatus(1L, user, "Completed");

        assertFalse(result);
    }

    // ================== COUNT TASKS ==================

    /**
     * Test: Count total tasks for a user
     */
    @Test
    void testCountTasksByUser() {

        User user = new User();
        user.setId(1L);

        List<Task> tasks = List.of(new Task(), new Task());

        when(repo.findByAssignedTo(user)).thenReturn(tasks);

        int count = taskService.countTasksByUser(user);

        assertEquals(2, count);
    }

    /**
     * Test: Count tasks by specific status
     *
     * Expected:
     * - Only matching status is counted
     */
    @Test
    void testCountTasksByUserAndStatus() {

        User user = new User();
        user.setId(1L);

        Task t1 = new Task();
        t1.setStatus("Completed");

        Task t2 = new Task();
        t2.setStatus("Pending");

        when(repo.findByAssignedTo(user)).thenReturn(List.of(t1, t2));

        int count = taskService.countTasksByUserAndStatus(user, "Completed");

        assertEquals(1, count);
    }
}