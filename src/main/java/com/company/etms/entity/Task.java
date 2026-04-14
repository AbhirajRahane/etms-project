package com.company.etms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class representing a Task.
 *
 * This class is mapped to the "task" table in the database.
 * Each task:
 * - Has a title, description, and status
 * - Is assigned to a user (employee)
 * - Tracks creation and update timestamps
 */
@Entity
@Table(name = "task")
public class Task {

    // Primary key (auto-incremented)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short title of the task
    private String title;

    // Detailed description of the task
    private String description;

    // Current status of the task (default: Pending)
    private String status = "Pending";

    /**
     * Many tasks can be assigned to one user.
     * This creates a foreign key "assigned_to" in the task table.
     */
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    // Timestamp when task is created
    private LocalDateTime createdAt = LocalDateTime.now();

    // Timestamp when task is last updated
    private LocalDateTime updatedAt;

    // ---------------- GETTERS & SETTERS ----------------

    /**
     * Get task ID
     * @return task ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set task ID
     * @param id task ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get task title
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set task title
     * @param title task title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get task description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set task description
     * @param description task details
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get task status
     * @return status (Pending, In Progress, Completed)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set task status
     *
     * @param status task status
     * Example values:
     * - Pending
     * - In Progress
     * - Completed
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get assigned user
     * @return User object
     */
    public User getAssignedTo() {
        return assignedTo;
    }

    /**
     * Assign task to a user
     * @param assignedTo user (employee)
     */
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Get creation timestamp
     * @return createdAt time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set creation timestamp
     * @param createdAt time when task was created
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get last updated timestamp
     * @return updatedAt time
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set last updated timestamp
     * @param updatedAt last update time
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}