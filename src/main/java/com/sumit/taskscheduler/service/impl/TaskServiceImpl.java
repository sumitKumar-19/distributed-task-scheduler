package com.sumit.taskscheduler.service.impl;

import com.sumit.taskscheduler.dto.CreateTaskRequest;
import com.sumit.taskscheduler.dto.TaskResponse;
import com.sumit.taskscheduler.dto.UpdateTaskRequest;
import com.sumit.taskscheduler.entity.Task;
import com.sumit.taskscheduler.repository.TaskRepository;
import com.sumit.taskscheduler.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        log.info("Creating new task: {}", request.getName());
        // TODO: check here, how you can improve.
        Task task = new Task();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setCronExpression(request.getCronExpression());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setMaxRetries(request.getMaxRetries());
        task.setStatus("ACTIVE");

        // For now, set next execution to 1 minute from now
        // We'll improve this with real cron parsing later
        task.setNextExecutionTime(LocalDateTime.now().plusMinutes(1));

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        return mapToResponse(savedTask);

    }

    public TaskResponse getTaskById(Long id) {
        log.info("Fetching task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));
        return mapToResponse(task);
    }

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.info("Fetching all tasks with pagination");
        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public List<TaskResponse> getTasksByStatus(String status) {
        log.info("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        log.info("Updating task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));

        if (request.getName() != null) {
            task.setName(request.getName());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getCronExpression() != null) {
            task.setCronExpression(request.getCronExpression());
            task.setNextExecutionTime(LocalDateTime.now().plusMinutes(1));
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getMaxRetries() != null) {
            task.setMaxRetries(request.getMaxRetries());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully");

        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with ID: " + id);
        }
        taskRepository.deleteById(id);
        log.info("Task deleted successfully");
    }

    @Transactional
    public TaskResponse pauseTask(Long id) {
        log.info("Pausing task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));

        task.setStatus("PAUSED");
        Task updatedTask = taskRepository.save(task);
        log.info("Task paused successfully");

        return mapToResponse(updatedTask);
    }

    @Transactional
    public TaskResponse resumeTask(Long id) {
        log.info("Resuming task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + id));

        task.setStatus("ACTIVE");
        Task updatedTask = taskRepository.save(task);
        log.info("Task resumed successfully");

        return mapToResponse(updatedTask);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .cronExpression(task.getCronExpression())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
                .status(task.getStatus())
                .retryCount(task.getRetryCount())
                .maxRetries(task.getMaxRetries())
                .nextExecutionTime(task.getNextExecutionTime())
                .lastExecutionTime(task.getLastExecutionTime())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }


}
