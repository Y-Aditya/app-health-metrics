package com.crc.healthmetrics.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Value("${scheduler.connectionpool.size:1}")
    private Integer threadPoolSize;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(poolScheduler());
    }

    @Bean
    public ThreadPoolTaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(threadPoolSize);
        scheduler.setThreadNamePrefix("metrics-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public ISystemMetrics getOSSpecificMetricsCollector(Environment environment) {
        ISystemMetrics osCollector = null;

        Optional<String> os = Optional.ofNullable(environment.getProperty("os.name"));
        if (!os.isEmpty()) {
            String operatingSystem = os.get().toLowerCase();
            boolean windows = operatingSystem.contains("win");
            boolean linux = operatingSystem.contains("nix") || operatingSystem.contains("nux");
            boolean mac = operatingSystem.contains("mac");

            if (windows) {
                osCollector = new WindowsSystemMetrics();
            } else if (linux || mac){
                osCollector = new LinuxSystemMetrics();
            }
        }
        if (osCollector == null) {
            throw new UnsupportedOperationException("Unsupported OS:" + os);
        }

        return osCollector;
    }

}
