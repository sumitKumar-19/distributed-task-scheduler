package com.sumit.taskscheduler.scheduler.impl;

import com.sumit.taskscheduler.entity.Task;
import com.sumit.taskscheduler.executor.TaskExecutionEngine;
import com.sumit.taskscheduler.repository.TaskRepository;
import com.sumit.taskscheduler.scheduler.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerServiceImpl implements TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final TaskExecutionEngine executionEngine;

    /**
     * Polls the database every 30 seconds to find and execute due tasks
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void pollAndExecuteTasks() {
        log.debug("🔍 Polling for due tasks...");

        LocalDateTime now = LocalDateTime.now();
        List<Task> dueTasks = taskRepository.findDueTasks(now);

        if (dueTasks.isEmpty()) {
            log.debug("No tasks due for execution at {}", now);
            return;
        }

        log.info("📋 Found {} task(s) due for execution", dueTasks.size());

        // Execute all due tasks in parallel using thread pool
        List<CompletableFuture<Void>> futures = dueTasks.stream()
                .map(task -> executionEngine.executeAsync(task))
                .toList();

        // Log thread pool statistics
        TaskExecutionEngine.ExecutorStats stats = executionEngine.getStats();
        log.info("📊 Thread Pool Stats: {}", stats);

        // Optional: Wait for all tasks to complete (non-blocking)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("✅ All {} tasks submitted for execution", dueTasks.size()))
                .exceptionally(ex -> {
                    log.error("❌ Error during task execution: {}", ex.getMessage());
                    return null;
                });
    }
}
