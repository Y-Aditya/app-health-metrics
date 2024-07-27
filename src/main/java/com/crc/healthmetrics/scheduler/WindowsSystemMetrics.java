package com.crc.healthmetrics.scheduler;

import com.crc.healthmetrics.exception.OpenFilesException;
import com.crc.healthmetrics.exception.DiskUsageComputeException;
import com.crc.healthmetrics.exception.CPUUsageComputeException;

import com.crc.healthmetrics.util.ResourceExtractor;
import java.lang.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.http.HttpStatus;

import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class WindowsSystemMetrics implements ISystemMetrics {

    private static final Logger logger = LoggerFactory.getLogger(WindowsSystemMetrics.class);

    private static final String HANDLE_PATH = "bin/handle.exe";
    @Value("${process.wait.timeout:30}")
    private Integer processTimeOut;

    private FileStore fileStore;

    public WindowsSystemMetrics() {
        initializeFileStore();
    }

    private void initializeFileStore() {
        try {
            this.fileStore = Files.getFileStore(Paths.get("."));
        } catch (Exception e) {
            logger.error("File Store fetch Exception: {}", e.getMessage());
            this.fileStore = null;
        }
    }

    @Override
    public double getCpuUsage() {
        return getProcessCpuLoad();
    }

    private double getProcessCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean osBeanImpl) {
            return osBeanImpl.getProcessCpuLoad() * 100;
        } else {
            throw new CPUUsageComputeException("CPU Usage Compute Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public double getTotalDiskUsage() {
        return getDiskUsage();
    }

    private double getDiskUsage() {
        try {
            long totalSpace = fileStore.getTotalSpace();
            long usableSpace = fileStore.getUsableSpace();
            return ((double)(totalSpace - usableSpace) / totalSpace) * 100;
        } catch (Exception e) {
            throw new DiskUsageComputeException("Disk Usage Compute Error:"+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public int getOpenFiles() {
        int openFilesCount = 0;
        Process process = null;
        try {
            String pid = new ApplicationPid().toString();
            File handleFile = ResourceExtractor.extractResourceToTempFile(HANDLE_PATH);
            ProcessBuilder processBuilder = new ProcessBuilder(handleFile.getAbsolutePath(),  "-accepteula", "-p", pid);
            process = processBuilder.start();

            // Use try-with-resources to ensure BufferedReader is closed
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("File")) {
                        openFilesCount++;
                    }
                }

                // Log errors from the error stream if necessary
                while ((line = errorReader.readLine()) != null) {
                    logger.error("Error: {}", line);
                }
            }

            // Wait for a maximum of 30 seconds and Destroy the process if it takes too long
            if (!process.waitFor(processTimeOut, TimeUnit.SECONDS)) {
                process.destroy();
                throw new OpenFilesException("Process timed out", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (IOException e) {
            throw new OpenFilesException("Failed to execute handle.exe to get open files:"+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (SecurityException e) {
            throw new OpenFilesException("Security exception while executing handle.exe:"+ e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new OpenFilesException("Process was interrupted:"+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new OpenFilesException("Unexpected error while getting open files:"+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return openFilesCount;
    }
}
