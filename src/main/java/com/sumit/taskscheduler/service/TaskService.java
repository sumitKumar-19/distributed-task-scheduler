package com.sumit.taskscheduler.service;

import com.sumit.taskscheduler.dto.CreateTaskRequest;
import com.sumit.taskscheduler.dto.ExecutionHistoryResponse;
import com.sumit.taskscheduler.dto.TaskResponse;
import com.sumit.taskscheduler.dto.UpdateTaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse getTaskById(Long id);

    Page<TaskResponse> getAllTasks(Pageable pageable);

    List<TaskResponse> getTasksByStatus(String status);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    void deleteTask(Long id);

    TaskResponse pauseTask(Long id);

    TaskResponse resumeTask(Long id);

    List<ExecutionHistoryResponse> getTaskExecutionHistory(Long taskId);

}
