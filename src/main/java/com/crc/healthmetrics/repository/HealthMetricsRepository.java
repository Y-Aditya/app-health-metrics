package com.crc.healthmetrics.repository;

import com.crc.healthmetrics.entity.HealthMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface HealthMetricsRepository extends JpaRepository<HealthMetrics, Long> {
    Page<HealthMetrics> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
