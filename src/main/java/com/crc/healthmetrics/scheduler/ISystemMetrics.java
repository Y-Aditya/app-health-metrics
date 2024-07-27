package com.crc.healthmetrics.scheduler;

public interface ISystemMetrics {
    double getCpuUsage();
    double getTotalDiskUsage();
    int getOpenFiles();
}