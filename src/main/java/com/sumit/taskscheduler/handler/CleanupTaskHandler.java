package com.sumit.taskscheduler.handler;

import com.sumit.taskscheduler.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j

public class CleanupTaskHandler implements TaskHandler {

    @Override
    public void execute(Task task) throws Exception {
        log.info("🧹 [CLEANUP HANDLER] Executing task: {}", task.getName());

        // Simulate cleanup operations
        log.info("   → Scanning for old log files...");
        Thread.sleep(400);

        log.info("   → Deleting files older than 30 days...");
        Thread.sleep(600);

        log.info("   → Compressing archived data...");
        Thread.sleep(500);

        log.info("   ✅ Cleanup completed successfully!");
    }

    @Override
    public String getTaskType() {
        return "CLEANUP";
    }
}
