package com.sumit.taskscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private String cronExpression;
    private String taskType;
    private String priority;
    private String status;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
