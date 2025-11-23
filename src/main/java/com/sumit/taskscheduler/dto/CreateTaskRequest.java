package com.sumit.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task name is required")
    private String name;

    private String description;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotBlank(message = "Task type is required")
    private String taskType;

    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW

    private Integer maxRetries = 3;
}
