package io.github.Stiofain_MacMathuna.telemetry_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "telemetry_events")
@Data
public class TelemetryEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("sensorId")
    private String sensorId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("fillNumber")
    private Integer fillNumber;

    @JsonProperty("accelerator")
    private String accelerator;

    private LocalDateTime timestamp;

    @Min(value = 0, message = "Beam intensity cannot be negative")
    private Double value; // This stores the Intensity

    @JsonProperty("status")
    private String status;

    @JsonProperty("energy")
    private Double energy;
}