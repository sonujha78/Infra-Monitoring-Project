package com.company.employeeapp.controller;

import com.company.employeeapp.service.MonitoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/monitor")
    public String monitor() {

        double cpu = monitoringService.getCpuUsage();
        double ram = monitoringService.getRamUsage();

        return "CPU Usage: " + String.format("%.2f", cpu) +
                "%<br>RAM Usage: " +
                String.format("%.2f", ram) + "%";
    }
}
