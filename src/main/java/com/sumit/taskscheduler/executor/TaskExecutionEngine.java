package com.sumit.taskscheduler.executor;

import com.sumit.taskscheduler.entity.Task;
import com.sumit.taskscheduler.entity.TaskExecutionHistory;
import com.sumit.taskscheduler.handler.TaskHandler;
import com.sumit.taskscheduler.repository.TaskExecutionHistoryRepository;
import com.sumit.taskscheduler.repository.TaskRepository;
import com.sumit.taskscheduler.util.CronExpressionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class TaskExecutionEngine {

    private final TaskRepository taskRepository;
    private final TaskExecutionHistoryRepository executionHistoryRepository;
    private final List<TaskHandler> taskHandlers;

    private ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 10;

    public TaskExecutionEngine(
            TaskRepository taskRepository,
            TaskExecutionHistoryRepository executionHistoryRepository,
            List<TaskHandler> taskHandlers) {
        this.taskRepository = taskRepository;
        this.executionHistoryRepository = executionHistoryRepository;
        this.taskHandlers = taskHandlers;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing Task Execution Engine with thread pool size: {}", THREAD_POOL_SIZE);

        // Create thread pool with custom thread factory for better debugging
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("TaskExecutor-" + (++counter));
                thread.setDaemon(false);
                return thread;
            }
        };

        executorService = new ThreadPoolExecutor(
                THREAD_POOL_SIZE,           // Core pool size
                THREAD_POOL_SIZE,           // Maximum pool size
                60L,                        // Keep alive time
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );

        log.info("Task Execution Engine initialized successfully");
    }

    /**
     * Execute task asynchronously using thread pool
     */
    public CompletableFuture<Void> executeAsync(Task task) {
        return CompletableFuture.runAsync(() -> {
            try {
                executeTask(task);
            } catch (Exception e) {
                log.error("Error in async task execution for task {}: {}",
                        task.getId(), e.getMessage(), e);
                throw new CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Execute task synchronously (called by thread pool)
     */
    private void executeTask(Task task) {
        String threadName = Thread.currentThread().getName();
        log.info("🚀 [{}] Starting execution of task [ID: {}, Name: {}, Type: {}]",
                threadName, task.getId(), task.getName(), task.getTaskType());

        long startTime = System.currentTimeMillis();
        TaskExecutionHistory history = TaskExecutionHistory.builder()
                .taskId(task.getId())
                .executionTime(LocalDateTime.now())
                .status("RUNNING")
                .build();

        try {
            // Save execution start
            history = executionHistoryRepository.save(history);
            log.debug("[{}] Execution history record created with ID: {}", threadName, history.getId());

            // Find appropriate handler
            TaskHandler handler = findHandler(task);
            log.debug("[{}] Using handler: {}", threadName, handler.getClass().getSimpleName());

            // Execute task using handler
            handler.execute(task);

            // Calculate execution duration
            long duration = System.currentTimeMillis() - startTime;

            // Mark as successful
            history.setStatus("SUCCESS");
            history.setExecutionDurationMs(duration);
            executionHistoryRepository.save(history);

            // Update task for next execution
            updateTaskAfterExecution(task, true);

            log.info("✅ [{}] Task {} executed successfully in {}ms",
                    threadName, task.getId(), duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Mark as failed
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            history.setExecutionDurationMs(duration);
            executionHistoryRepository.save(history);

            // Update task after failure
            updateTaskAfterExecution(task, false);

            log.error("❌ [{}] Task {} execution failed after {}ms: {}",
                    threadName, task.getId(), duration, e.getMessage());
        }
    }

    /**
     * Find appropriate handler for the task
     */
    private TaskHandler findHandler(Task task) {
        return taskHandlers.stream()
                .filter(handler -> handler.supports(task))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No handler found for task type: " + task.getTaskType()));
    }

    /**
     * Update task after execution
     */
    private void updateTaskAfterExecution(Task task, boolean success) {
        try {
            // Reload task from database to avoid stale data
            Task freshTask = taskRepository.findById(task.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found: " + task.getId()));

            // Update last execution time
            freshTask.setLastExecutionTime(LocalDateTime.now());

            // Reset retry count on success
            if (success) {
                freshTask.setRetryCount(0);
            }

            // Calculate next execution time from cron
            try {
                LocalDateTime nextExecution = CronExpressionUtil.getNextExecutionTime(
                        freshTask.getCronExpression()
                );
                freshTask.setNextExecutionTime(nextExecution);
                log.debug("Task {} next execution scheduled for: {}", freshTask.getId(), nextExecution);
            } catch (Exception e) {
                log.error("Failed to calculate next execution time for task {}: {}",
                        freshTask.getId(), e.getMessage());
                // Set to 1 hour from now as fallback
                freshTask.setNextExecutionTime(LocalDateTime.now().plusHours(1));
            }

            taskRepository.save(freshTask);
        } catch (Exception e) {
            log.error("Error updating task after execution: {}", e.getMessage(), e);
        }
    }

    /**
     * Get thread pool statistics
     */
    public ExecutorStats getStats() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            return new ExecutorStats(
                    tpe.getActiveCount(),
                    tpe.getPoolSize(),
                    tpe.getQueue().size(),
                    tpe.getCompletedTaskCount()
            );
        }
        return new ExecutorStats(0, 0, 0, 0);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Task Execution Engine...");

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate in time, forcing shutdown...");
                    executorService.shutdownNow();
                }
                log.info("Task Execution Engine shut down successfully");
            } catch (InterruptedException e) {
                log.error("Error during executor shutdown", e);
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Inner class to hold executor statistics
     */
    public static class ExecutorStats {
        public final int activeThreads;
        public final int poolSize;
        public final int queueSize;
        public final long completedTasks;

        public ExecutorStats(int activeThreads, int poolSize, int queueSize, long completedTasks) {
            this.activeThreads = activeThreads;
            this.poolSize = poolSize;
            this.queueSize = queueSize;
            this.completedTasks = completedTasks;
        }

        @Override
        public String toString() {
            return String.format("Active: %d, Pool: %d, Queue: %d, Completed: %d",
                    activeThreads, poolSize, queueSize, completedTasks);
        }
    }
}