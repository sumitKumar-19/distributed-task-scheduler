package com.sumit.taskscheduler.handler;

import com.sumit.taskscheduler.entity.Task;

/**
 * Interface for handling different types of tasks
 * Strategy Pattern implementation
 */

public interface TaskHandler {
    /**
     * Execute the task
     *
     * @param task Task to execute
     * @throws Exception if execution fails
     */
    void execute(Task task) throws Exception;

    /**
     * Get the task type this handler supports
     *
     * @return Task type (e.g., "EMAIL", "REPORT")
     */
    String getTaskType();

    /**
     * Check if this handler supports the given task
     *
     * @param task Task to check
     * @return true if supported
     */
    default boolean supports(Task task) {
        return task.getTaskType().equalsIgnoreCase(getTaskType());
    }
}
