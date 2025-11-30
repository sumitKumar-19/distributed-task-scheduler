package com.sumit.taskscheduler.handler;

import com.sumit.taskscheduler.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailTaskHandler implements TaskHandler {

    @Override
    public void execute(Task task) throws Exception {
        log.info("📧 [EMAIL HANDLER] Executing task: {}", task.getName());

        // Simulate email sending
        log.info("   → Preparing email template...");
        Thread.sleep(300);

        log.info("   → Connecting to SMTP server...");
        Thread.sleep(200);

        log.info("   → Sending email to recipients...");
        Thread.sleep(500);

        log.info("   ✅ Email sent successfully!");
    }

    @Override
    public String getTaskType() {
        return "EMAIL";
    }
}
