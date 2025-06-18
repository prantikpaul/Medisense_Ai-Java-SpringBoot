package com.example.medisense_ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_metrics")
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column
    private Double weight; // in kg

    @Column
    private Double bloodPressureSystolic; // in mmHg

    @Column
    private Double bloodPressureDiastolic; // in mmHg

    @Column
    private Integer heartRate; // in bpm

    @Column
    private Double bloodGlucose; // in mg/dL

    @Column
    private Double cholesterolTotal; // in mg/dL

    @Column
    private Double cholesterolHDL; // in mg/dL

    @Column
    private Double cholesterolLDL; // in mg/dL

    @Column
    private Integer sleepHours; // hours of sleep

    @Column
    private Integer steps; // number of steps

    @Column
    private String notes;

    // Pre-persist hook to set the recorded time if not set
    @PrePersist
    public void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}