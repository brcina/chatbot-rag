package com.chatbotrag.tools.service;

import jakarta.inject.Singleton;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class SystemInfoTool {
    
    private final SystemInfo systemInfo = new SystemInfo();
    
    public Mono<Map<String, Object>> getSystemInfo() {
        return Mono.fromCallable(() -> {
            Map<String, Object> info = new HashMap<>();
            
            HardwareAbstractionLayer hardware = systemInfo.getHardware();
            OperatingSystem os = systemInfo.getOperatingSystem();
            
            // OS Information
            info.put("os.name", os.getFamily());
            info.put("os.version", os.getVersionInfo().toString());
            info.put("os.manufacturer", os.getManufacturer());
            
            // CPU Information
            var processor = hardware.getProcessor();
            info.put("cpu.name", processor.getProcessorIdentifier().getName());
            info.put("cpu.cores", processor.getLogicalProcessorCount());
            
            // Get current CPU usage (requires two measurements)
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
            info.put("cpu.usage", cpuUsage);
            
            // Memory Information
            var memory = hardware.getMemory();
            info.put("memory.total", memory.getTotal());
            info.put("memory.available", memory.getAvailable());
            info.put("memory.used", memory.getTotal() - memory.getAvailable());
            
            return info;
        });
    }
    
    public Mono<Double> getCpuUsage() {
        return Mono.fromCallable(() -> {
            var processor = systemInfo.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 0.0;
            }
            return processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        });
    }
    
    public Mono<Map<String, Long>> getMemoryInfo() {
        return Mono.fromCallable(() -> {
            var memory = systemInfo.getHardware().getMemory();
            Map<String, Long> memInfo = new HashMap<>();
            memInfo.put("total", memory.getTotal());
            memInfo.put("available", memory.getAvailable());
            memInfo.put("used", memory.getTotal() - memory.getAvailable());
            return memInfo;
        });
    }
    
    public Mono<String> getOSInfo() {
        return Mono.fromCallable(() -> {
            OperatingSystem os = systemInfo.getOperatingSystem();
            return String.format("%s %s (%s)", 
                os.getFamily(), 
                os.getVersionInfo().toString(),
                os.getManufacturer());
        });
    }
}