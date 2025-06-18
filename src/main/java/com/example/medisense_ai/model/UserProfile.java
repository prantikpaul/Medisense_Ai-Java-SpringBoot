package com.example.medisense_ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id; // Same as user ID

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String profilePicture; // Path to profile picture

    @Column
    private String phoneNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private Double weight; // in kg

    @Column
    private String bloodType; // A+, B-, etc.

    @Column
    private String emergencyContactName;

    @Column
    private String emergencyContactPhone;

    @Column
    private String emergencyContactRelationship;

    @ElementCollection
    @CollectionTable(name = "user_allergies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergy")
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_medications", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "medication")
    private List<String> medications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_medical_conditions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "condition")
    private List<String> medicalConditions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_food_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference")
    private List<String> foodPreferences = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_food_dislikes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "dislike")
    private List<String> foodDislikes = new ArrayList<>();

    @Column
    private String fitnessGoals;

    @Column
    private String dietaryPreferences; // Vegetarian, Vegan, etc.

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private boolean emailNotifications;

    @Column
    private boolean smsNotifications;

    @Column
    private boolean appointmentReminders;

    @Column
    private boolean medicationReminders;

    @Column
    private boolean healthTips;

    @Column
    private boolean shareHealthData;

    @Column
    private boolean shareActivityData;

    @Column
    private boolean allowDataAnalysis;

    // Pre-persist hook
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Pre-update hook
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}