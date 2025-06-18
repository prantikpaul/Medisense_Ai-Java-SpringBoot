package com.example.medisense_ai.repository;

import com.example.medisense_ai.model.HealthMetric;
import com.example.medisense_ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    List<HealthMetric> findByUser(User user);
    List<HealthMetric> findByUserOrderByRecordedAtDesc(User user);
    List<HealthMetric> findByUserAndRecordedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}