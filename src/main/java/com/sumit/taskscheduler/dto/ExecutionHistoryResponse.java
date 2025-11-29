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
public class ExecutionHistoryResponse {
    private Long id;
    private Long taskId;
    private LocalDateTime executionTime;
    private String status;
    private String errorMessage;
    private Long executionDurationMs;
    private LocalDateTime createdAt;
}