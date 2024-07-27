package com.crc.healthmetrics.cachetest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import com.crc.healthmetrics.entity.HealthMetrics;
import com.crc.healthmetrics.repository.HealthMetricsRepository;
import com.crc.healthmetrics.service.HealthMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {HealthMetricsServiceTest.TestConfig.class, HealthMetricsService.class})
public class HealthMetricsServiceTest {

    @MockBean
    private HealthMetricsRepository healthMetricsRepository;

    @Autowired
    private HealthMetricsService healthMetricsService;

    @Autowired
    private CacheManager cacheManager;

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("metrics");
        }
    }

    @BeforeEach
    public void setUp() {
        cacheManager.getCache("metrics").clear();
    }

    @Test
    public void testCaching() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<HealthMetrics> page = new PageImpl<>(Collections.emptyList());

        when(healthMetricsRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
            .thenReturn(page);

        // Call the method twice with the same parameters
        healthMetricsService.getHealthMetrics(start, end, pageable);
        healthMetricsService.getHealthMetrics(start, end, pageable);

        // Verify the repository method is called only once due to caching
        verify(healthMetricsRepository, times(1)).findByTimestampBetween(start, end, pageable);
    }
}
