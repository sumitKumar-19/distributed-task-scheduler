package com.sumit.taskscheduler.handler;

import com.sumit.taskscheduler.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportTaskHandler implements TaskHandler {
    @Override
    public void execute(Task task) throws Exception {
        log.info("📊 [REPORT HANDLER] Executing task: {}", task.getName());

        // Simulate report generation
        log.info("   → Fetching data from database...");
        Thread.sleep(500);

        log.info("   → Processing data and calculating metrics...");
        Thread.sleep(800);

        log.info("   → Generating PDF report...");
        Thread.sleep(700);

        log.info("   ✅ Report generated successfully!");
    }

    @Override
    public String getTaskType() {
        return "REPORT";
    }
}
