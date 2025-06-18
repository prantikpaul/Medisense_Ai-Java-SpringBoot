package com.example.medisense_ai.service;

import com.example.medisense_ai.model.HealthMetric;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.repository.HealthMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;

    @Autowired
    public HealthMetricService(HealthMetricRepository healthMetricRepository) {
        this.healthMetricRepository = healthMetricRepository;
    }

    public HealthMetric saveHealthMetric(HealthMetric healthMetric) {
        return healthMetricRepository.save(healthMetric);
    }

    public Optional<HealthMetric> findById(Long id) {
        return healthMetricRepository.findById(id);
    }

    public List<HealthMetric> findAllByUser(User user) {
        return healthMetricRepository.findByUser(user);
    }

    public List<HealthMetric> findRecentMetricsByUser(User user) {
        return healthMetricRepository.findByUserOrderByRecordedAtDesc(user);
    }

    public List<HealthMetric> findMetricsByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return healthMetricRepository.findByUserAndRecordedAtBetween(user, start, end);
    }

    public void deleteHealthMetric(Long id) {
        healthMetricRepository.deleteById(id);
    }
}