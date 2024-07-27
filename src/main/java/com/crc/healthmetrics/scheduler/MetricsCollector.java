package com.crc.healthmetrics.scheduler;

import com.crc.healthmetrics.entity.HealthMetrics;
import com.crc.healthmetrics.exception.MetricsCaptureException;
import com.crc.healthmetrics.repository.HealthMetricsRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    private volatile boolean isTaskRunning = false;
    private ScheduledFuture<?> scheduledFuture;

    private final ISystemMetrics metricsService;
    private final HealthMetricsRepository healthMetricsRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    @Value("${metrics.collection.interval:60000}")
    private long fixedRate;

    public MetricsCollector(ISystemMetrics metricsService, HealthMetricsRepository healthMetricsRepository, ThreadPoolTaskScheduler taskScheduler) {
        this.metricsService = metricsService;
        this.healthMetricsRepository = healthMetricsRepository;
        this.taskScheduler = taskScheduler;
    }

    @Scheduled(fixedRateString = "${metrics.collection.interval:60000}")
    public void scheduledTask() {
        if (isTaskRunning) {
            try {
                HealthMetrics metrics = new HealthMetrics();
                metrics.setTimestamp(LocalDateTime.now());
                metrics.setCpuUsage(metricsService.getCpuUsage());
                metrics.setDiskUsage(metricsService.getTotalDiskUsage());
                metrics.setOpenFiles(metricsService.getOpenFiles());

                healthMetricsRepository.save(metrics);
                logger.info("Metrics collected and saved successfully.");
            } catch (Exception e) {
                throw new MetricsCaptureException("Exception Metrics Capture by Scheduler: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.info("Scheduled task is currently disabled.");
        }
    }

    public void startTask() {
        if (scheduledFuture == null || scheduledFuture.isCancelled()) {
            isTaskRunning = true;
            Instant startTime = Instant.now(); // Start time as current instant
            Duration delay = Duration.ofMillis(fixedRate); // Delay duration
            scheduledFuture = taskScheduler.scheduleWithFixedDelay(this::scheduledTask, startTime, delay);

            logger.info("Scheduled task enabled.");
        } else {
            logger.info("Scheduled task is already running.");
        }
    }

    public void stopTask() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
            isTaskRunning = false;
            logger.info("Scheduled task disabled.");
        } else {
            logger.info("No running scheduled task to disable.");
        }
    }

    @PreDestroy
    public void shutdown() {
        stopTask();
        taskScheduler.shutdown();
        try {
            if (!taskScheduler.getScheduledExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
                taskScheduler.getScheduledExecutor().shutdownNow();
            }
            logger.info("Scheduler shut down gracefully.");
        } catch (InterruptedException ex) {
            taskScheduler.getScheduledExecutor().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
