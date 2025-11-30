package com.sumit.taskscheduler.controller;

import com.sumit.taskscheduler.dto.CreateTaskRequest;
import com.sumit.taskscheduler.dto.ExecutionHistoryResponse;
import com.sumit.taskscheduler.dto.TaskResponse;
import com.sumit.taskscheduler.dto.UpdateTaskRequest;
import com.sumit.taskscheduler.executor.TaskExecutionEngine;
import com.sumit.taskscheduler.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final TaskExecutionEngine executionEngine;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Request received to create task: {}", request.getName());
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        log.info("Received request to get task: {}", id);
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Received request to get all tasks, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable String status) {
        log.info("Received request to get tasks by status: {}", status);
        List<TaskResponse> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("Received request to update task: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Received request to delete task: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<TaskResponse> pauseTask(@PathVariable Long id) {
        log.info("Received request to pause task: {}", id);
        TaskResponse response = taskService.pauseTask(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<TaskResponse> resumeTask(@PathVariable Long id) {
        log.info("Received request to resume task: {}", id);
        TaskResponse response = taskService.resumeTask(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ExecutionHistoryResponse>> getTaskExecutionHistory(@PathVariable Long id) {
        log.info("REST request to get execution history for task: {}", id);
        List<ExecutionHistoryResponse> history = taskService.getTaskExecutionHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/executor/stats")
    public ResponseEntity<TaskExecutionEngine.ExecutorStats> getExecutorStats() {
        log.info("REST request to get executor statistics");
        TaskExecutionEngine.ExecutorStats stats = executionEngine.getStats();
        return ResponseEntity.ok(stats);
    }

}
