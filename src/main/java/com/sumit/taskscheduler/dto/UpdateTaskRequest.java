package com.sumit.taskscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private String name;
    private String description;
    private String cronExpression;
    private String priority;
    private Integer maxRetries;
}
