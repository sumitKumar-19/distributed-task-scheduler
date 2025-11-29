package com.sumit.taskscheduler.repository;

import com.sumit.taskscheduler.entity.TaskExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskExecutionHistoryRepository extends JpaRepository<TaskExecutionHistory, Long> {

    // Find all executions for a specific task
    List<TaskExecutionHistory> findByTaskIdOrderByExecutionTimeDesc(Long taskId);

    // Find executions for a task with pagination
    Page<TaskExecutionHistory> findByTaskId(Long taskId, Pageable pageable);

    // Find recent executions (last N records)
    List<TaskExecutionHistory> findTop10ByTaskIdOrderByExecutionTimeDesc(Long taskId);

    // Find failed executions
    List<TaskExecutionHistory> findByStatusOrderByExecutionTimeDesc(String status);

    // Count executions for a task
    long countByTaskId(Long taskId);

    // Get execution statistics
    @Query("SELECT COUNT(h) FROM TaskExecutionHistory h WHERE h.taskId = :taskId AND h.status = :status")
    long countByTaskIdAndStatus(@Param("taskId") Long taskId, @Param("status") String status);

    // Find executions in a date range
    List<TaskExecutionHistory> findByExecutionTimeBetween(LocalDateTime start, LocalDateTime end);
}