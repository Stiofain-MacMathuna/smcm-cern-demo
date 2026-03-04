package io.github.Stiofain_MacMathuna.telemetry_service;

import io.github.Stiofain_MacMathuna.telemetry_service.model.TelemetryEvent;
import io.github.Stiofain_MacMathuna.telemetry_service.repository.TelemetryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("api/v1/telemetry")
@CrossOrigin(origins = "*")
public class TelemetryController {

    @Autowired
    private TelemetryRepository repository;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PostMapping("/ingest")
    public String ingestData(@RequestBody TelemetryEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        executor.submit(() -> {
            System.out.println("[POST-MORTEM] Analyzing " + event.getEventType() +
                    " from Sensor: " + event.getSensorId());

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            repository.save(event);

            System.out.println("[ARCHIVE] Event from " + event.getSensorId() + " successfully persisted to DB.");
        });

        return "CERN Telemetry Ingested: Event queued for analysis and persistence.";
    }

    @GetMapping("/history")
    public List<TelemetryEvent> getHistory() {
        /**
         * FIX: Switched from findAll() to findTop100ByOrderByTimestampDesc()
         * This prevents the java.lang.OutOfMemoryError by capping the data
         * returned to the frontend.
         */
        return repository.findTop100ByOrderByTimestampDesc();
    }

    @GetMapping("/sensor/{sensorId}")
    public List<TelemetryEvent> getBySensor(@PathVariable String sensorId) {
        return repository.findBySensorId(sensorId);
    }

    @DeleteMapping("/history")
    public ResponseEntity<String> clearHistory() {
        repository.deleteAll();
        return ResponseEntity.ok("Post-Mortem buffer cleared. System ready for new fill.");
    }
}