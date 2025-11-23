package com.sumit.taskscheduler.service;

import com.sumit.taskscheduler.dto.CreateTaskRequest;
import com.sumit.taskscheduler.dto.TaskResponse;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);
}
