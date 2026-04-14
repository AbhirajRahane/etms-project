package com.company.etms.dto;

/**
 * Data Transfer Object (DTO) for Task.
 *
 * Used to transfer basic task information (title and status)
 * between layers without exposing the full Task entity.
 */
public class TaskDto {

    private String title;
    private String status;

    /**
     * Gets the title of the task.
     *
     * @return Task title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the task.
     *
     * @param title Task title
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the status of the task.
     *
     * @return Task status
     */
    public String getStatus() { return status; }

    /**
     * Sets the status of the task.
     *
     * @param status Task status
     */
    public void setStatus(String status) { this.status = status; }
}