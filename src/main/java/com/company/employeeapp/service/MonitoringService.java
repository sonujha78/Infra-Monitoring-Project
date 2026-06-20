package com.company.employeeapp.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Service
public class MonitoringService {

    public double getCpuUsage() {
        OperatingSystemMXBean os =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        return os.getCpuLoad() * 100;
    }

    public double getRamUsage() {
        OperatingSystemMXBean os =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long total = os.getTotalMemorySize();
        long free = os.getFreeMemorySize();

        return ((double)(total - free) / total) * 100;
    }
}
