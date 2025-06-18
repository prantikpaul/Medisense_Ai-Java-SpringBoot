package com.example.medisense_ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private boolean emailNotifications;

    @Column(nullable = false)
    private boolean smsNotifications;

    @Column(nullable = false)
    private boolean appointmentReminders;

    @Column(nullable = false)
    private boolean medicationReminders;

    @Column(nullable = false)
    private boolean healthTips;

    @Column(nullable = false)
    private boolean shareHealthData;

    @Column(nullable = false)
    private boolean shareActivityData;

    @Column(nullable = false)
    private boolean allowDataAnalysis;

    // Additional health-related fields can be added here
}