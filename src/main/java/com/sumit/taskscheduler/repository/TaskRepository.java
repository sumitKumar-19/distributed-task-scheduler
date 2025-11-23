package com.sumit.taskscheduler.repository;

import com.sumit.taskscheduler.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all tasks with a specific status
    List<Task> findByStatus(String status);

    // Find tasks that are due for execution
    @Query("SELECT t FROM Task t WHERE t.nextExecutionTime <= :now AND t.status = 'ACTIVE' " +
            "ORDER BY t.priority DESC, t.nextExecutionTime ASC")
    List<Task> findDueTasks(@Param("now") LocalDateTime now);

    // Find tasks by type
    List<Task> findByTaskType(String taskType);

    // Find tasks by priority
    List<Task> findByPriority(String priority);

    // Search tasks by name (case-insensitive)
    Page<Task> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
