package com.sumit.taskscheduler.scheduler.impl;

import com.sumit.taskscheduler.entity.Task;
import com.sumit.taskscheduler.entity.TaskExecutionHistory;
import com.sumit.taskscheduler.repository.TaskExecutionHistoryRepository;
import com.sumit.taskscheduler.repository.TaskRepository;
import com.sumit.taskscheduler.util.CronExpressionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerServiceImpl {

    private final TaskRepository taskRepository;
    private final TaskExecutionHistoryRepository executionHistoryRepository;

    /**
     * Polls the database every 30 seconds to find and execute due tasks
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000) // 30 seconds delay, 10 seconds initial delay
    @Transactional
    public void pollAndExecuteTasks() {
        log.debug("Polling for due tasks...");

        LocalDateTime now = LocalDateTime.now();
        List<Task> dueTasks = taskRepository.findDueTasks(now);

        if (dueTasks.isEmpty()) {
            log.debug("No tasks due for execution at {}", now);
            return;
        }

        log.info("Found {} task(s) due for execution", dueTasks.size());

        for (Task task : dueTasks) {
            try {
                executeTask(task);
            } catch (Exception e) {
                log.error("Error executing task {}: {}", task.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Executes a single task
     */
    private void executeTask(Task task) throws InterruptedException {
        log.info("Executing task [ID: {}, Name: {}, Type: {}]",
                task.getId(), task.getName(), task.getTaskType());

        long startTime = System.currentTimeMillis();
        TaskExecutionHistory history = TaskExecutionHistory.builder()
                .taskId(task.getId())
                .executionTime(LocalDateTime.now())
                .status("RUNNING")
                .build();

        try {
            // Save execution start
            history = executionHistoryRepository.save(history);

            // Simulate task execution (we'll add real handlers later)
            performTaskExecution(task);

            // Calculate execution duration
            long duration = System.currentTimeMillis() - startTime;

            // Mark as successful
            history.setStatus("SUCCESS");
            history.setExecutionDurationMs(duration);
            executionHistoryRepository.save(history);

            // Update task for next execution
            updateTaskAfterExecution(task, true);

            log.info("Task {} executed successfully in {}ms", task.getId(), duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Mark as failed
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            history.setExecutionDurationMs(duration);
            executionHistoryRepository.save(history);

            // Update task after failure (for retry logic)
            updateTaskAfterExecution(task, false);

            log.error("Task {} execution failed: {}", task.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Performs the actual task execution logic
     * TODO: This will be replaced with proper task handlers in Day 4
     */
    private void performTaskExecution(Task task) throws InterruptedException {
        log.info(">>> Performing {} task: {}", task.getTaskType(), task.getName());

        // Simulate task execution with different durations based on type
        switch (task.getTaskType().toUpperCase()) {
            case "EMAIL":
                log.info("    📧 Sending email...");
                Thread.sleep(1000); // Simulate 1 second
                log.info("    ✅ Email sent successfully");
                break;

            case "REPORT":
                log.info("    📊 Generating report...");
                Thread.sleep(2000); // Simulate 2 seconds
                log.info("    ✅ Report generated successfully");
                break;

            case "CLEANUP":
                log.info("    🧹 Cleaning up old data...");
                Thread.sleep(1500); // Simulate 1.5 seconds
                log.info("    ✅ Cleanup completed");
                break;

            case "HEALTH_CHECK":
                log.info("    💊 Running health checks...");
                Thread.sleep(500); // Simulate 0.5 seconds
                log.info("    ✅ System healthy");
                break;

            default:
                log.info("    ⚙️ Executing generic task...");
                Thread.sleep(1000);
                log.info("    ✅ Task completed");
        }
    }

    /**
     * Updates task after execution
     */
    private void updateTaskAfterExecution(Task task, boolean success) {
        // Update last execution time
        task.setLastExecutionTime(LocalDateTime.now());

        // Reset retry count on success
        if (success) {
            task.setRetryCount(0);
        }

        // Calculate next execution time from cron
        try {
            LocalDateTime nextExecution = CronExpressionUtil.getNextExecutionTime(
                    task.getCronExpression()
            );
            task.setNextExecutionTime(nextExecution);
            log.info("Task {} next execution scheduled for: {}", task.getId(), nextExecution);
        } catch (Exception e) {
            log.error("Failed to calculate next execution time for task {}: {}",
                    task.getId(), e.getMessage());
            // Set to 1 hour from now as fallback
            task.setNextExecutionTime(LocalDateTime.now().plusHours(1));
        }

        taskRepository.save(task);
    }
}