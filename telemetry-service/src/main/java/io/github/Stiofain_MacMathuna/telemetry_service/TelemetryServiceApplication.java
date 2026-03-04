package io.github.Stiofain_MacMathuna.telemetry_service;

import io.github.Stiofain_MacMathuna.telemetry_service.repository.TelemetryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class TelemetryServiceApplication {

    @Autowired
    private TelemetryRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(TelemetryServiceApplication.class, args);
    }

    @Scheduled(fixedRate = 1800000)
    public void autoPruneOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        try {
            repository.deleteOldData(cutoff);
            System.out.println("[SYSTEM-MAINTENANCE] successfully pruned data older than: " + cutoff);
        } catch (Exception e) {
            System.err.println("[SYSTEM-ERROR] Failed to prune old telemetry data: " + e.getMessage());
        }
    }
}