package com.sumit.taskscheduler.util;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
public class CronExpressionUtil {

    /**
     * Validates if the given cron expression is valid
     *
     * @param cronExpression Cron expression to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateCronExpression(String cronExpression) {
        if (StringUtils.isBlank(cronExpression)) {
            return false;
        }

        try {
            new CronExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            log.error("Invalid cron expression: {}", cronExpression, e);
            return false;
        }
    }

    /**
     * Calculates the next execution time based on cron expression
     *
     * @param cronExpression Cron expression
     * @return Next execution time as LocalDateTime
     * @throws IllegalArgumentException if cron expression is invalid
     */
    public static LocalDateTime getNextExecutionTime(String cronExpression) {
        return getNextExecutionTime(cronExpression, LocalDateTime.now());
    }

    /**
     * Calculates the next execution time based on cron expression from a given date
     *
     * @param cronExpression Cron expression
     * @param fromDate Starting date/time
     * @return Next execution time as LocalDateTime
     * @throws IllegalArgumentException if cron expression is invalid
     */
    public static LocalDateTime getNextExecutionTime(String cronExpression, LocalDateTime fromDate) {
        if (!validateCronExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        try {
            CronExpression cron = new CronExpression(cronExpression);
            Date fromDateAsDate = Date.from(fromDate.atZone(ZoneId.systemDefault()).toInstant());
            Date nextDate = cron.getNextValidTimeAfter(fromDateAsDate);

            if (nextDate == null) {
                throw new IllegalStateException("No future execution time available for cron: " + cronExpression);
            }

            return LocalDateTime.ofInstant(nextDate.toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse cron expression: " + cronExpression, e);
        }
    }

    /**
     * Gets a human-readable description of the cron expression
     *
     * @param cronExpression Cron expression
     * @return Description string
     */
    public static String getDescription(String cronExpression) {
        if (!validateCronExpression(cronExpression)) {
            return "Invalid cron expression";
        }

        // Basic descriptions for common patterns
        switch (cronExpression) {
            case "0 0 * * * ?":
                return "Every hour";
            case "0 0 0 * * ?":
                return "Daily at midnight";
            case "0 0 9 * * ?":
                return "Daily at 9 AM";
            case "0 0 12 * * ?":
                return "Daily at noon";
            case "0 0 0 ? * 2":
                return "Every Monday at midnight";
            case "0 0 0 ? * 1":
                return "Every Sunday at midnight";
            case "0 0 0 1 * ?":
                return "First day of every month";
            case "0 */15 * * * ?":
                return "Every 15 minutes";
            case "0 */30 * * * ?":
                return "Every 30 minutes";
            default:
                return "Custom schedule: " + cronExpression;
        }
    }
}