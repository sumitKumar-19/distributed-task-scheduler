package com.sumit.taskscheduler.handler;

import com.sumit.taskscheduler.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HealthCheckTaskHandler implements TaskHandler {
    @Override
    public void execute(Task task) throws Exception {
        log.info("💊 [HEALTH CHECK HANDLER] Executing task: {}", task.getName());

        // Simulate health checks
        log.info("   → Checking database connection...");
        Thread.sleep(100);

        log.info("   → Checking Redis connection...");
        Thread.sleep(100);

        log.info("   → Checking API endpoints...");
        Thread.sleep(150);

        log.info("   → Verifying disk space...");
        Thread.sleep(150);

        log.info("   ✅ All systems healthy!");
    }

    @Override
    public String getTaskType() {
        return "HEALTH_CHECK";
    }
}
