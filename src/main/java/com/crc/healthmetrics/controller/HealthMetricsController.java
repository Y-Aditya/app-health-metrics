package com.crc.healthmetrics.controller;

import com.crc.healthmetrics.entity.HealthMetrics;
import com.crc.healthmetrics.scheduler.MetricsCollector;
import com.crc.healthmetrics.service.HealthMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/v1")
public class HealthMetricsController {

    private HealthMetricsService healthMetricsService;

    private PageableValidator pageableValidator;

    private MetricsCollector metricsCollector;
    @Autowired
    public HealthMetricsController(HealthMetricsService healthMetricsService, PageableValidator pageableValidator, MetricsCollector metricsCollector) {
        this.healthMetricsService = healthMetricsService;
        this.pageableValidator = pageableValidator;
        this.metricsCollector = metricsCollector;
    }

    @Operation(summary = "Get health metrics within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the health metrics",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthMetrics.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Health metrics not found",
                    content = @Content)})
    @GetMapping("/health-metrics")
    public ResponseEntity<Page<HealthMetrics>> getHealthMetrics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        // Validate Pageable parameters
        pageableValidator.validate(pageable);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        Page<HealthMetrics> metrics = healthMetricsService.getHealthMetrics(start, end, pageable);
        return ResponseEntity.ok(metrics);
    }
    @Operation(summary = "Start metrics collection task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics collection task started successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/metrics/start-capture")
    public ResponseEntity<Map<String,Object>> startTask() {
        metricsCollector.startTask();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Metrics collection task started successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Stop metrics collection task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics collection task stopped successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/metrics/stop-capture")
    public ResponseEntity<Map<String,Object>> stopTask() {
        metricsCollector.stopTask();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Metrics collection task stopped successfully");
        return ResponseEntity.ok(response);
    }
}
