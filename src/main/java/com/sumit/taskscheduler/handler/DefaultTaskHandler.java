package com.sumit.taskscheduler.handler;

import com.sumit.taskscheduler.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultTaskHandler implements TaskHandler {
    @Override
    public void execute(Task task) throws Exception {
        log.info("⚙️ [DEFAULT HANDLER] Executing task: {}", task.getName());

        // Generic task execution
        log.info("   → Processing generic task...");
        Thread.sleep(1000);

        log.info("   ✅ Task completed successfully!");
    }

    @Override
    public String getTaskType() {
        return "DEFAULT";
    }

    @Override
    public boolean supports(Task task) {
        // Default handler supports any task type
        return true;
    }
}
