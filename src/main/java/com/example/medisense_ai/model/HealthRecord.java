package com.example.medisense_ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_records")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String recordType; // e.g., Lab Report, Prescription, Vaccination, etc.

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column
    private LocalDate recordDate; // Date of the medical event

    @Column
    private String providerName; // Doctor or healthcare provider name

    @Column
    private String facilityName; // Hospital or clinic name

    @Column
    private String documentPath; // Path to stored document if any

    @Column
    private String documentType; // File type (PDF, JPG, etc.)

    @Column
    private String documentName; // Original filename

    @Column
    private Long documentSize; // File size in bytes

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime updatedAt;

    // Pre-persist hook
    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
    }

    // Pre-update hook
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}