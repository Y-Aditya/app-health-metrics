package com.crc.healthmetrics.service;

import com.crc.healthmetrics.entity.HealthMetrics;
import com.crc.healthmetrics.repository.HealthMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthMetricsService {
    private static final Logger logger = LoggerFactory.getLogger(HealthMetricsService.class);

    private final HealthMetricsRepository healthMetricsRepository;

    public HealthMetricsService(/*SystemMetricsService metricsService,*/ HealthMetricsRepository healthMetricsRepository) {
        this.healthMetricsRepository = healthMetricsRepository;
    }

    @Cacheable("metrics")
    public Page<HealthMetrics> getHealthMetrics(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        logger.info("Fetching data from repository for range {} to {}", start, end);
        return healthMetricsRepository.findByTimestampBetween(start, end, pageable);
    }

}
