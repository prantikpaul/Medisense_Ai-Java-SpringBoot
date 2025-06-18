package com.example.medisense_ai.repository;

import com.example.medisense_ai.model.HealthRecord;
import com.example.medisense_ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByUser(User user);
    List<HealthRecord> findByUserOrderByUploadedAtDesc(User user);
    List<HealthRecord> findByUserAndRecordType(User user, String recordType);
    List<HealthRecord> findByUserAndRecordDateBetween(User user, LocalDate start, LocalDate end);
    List<HealthRecord> findByUserAndTitleContainingIgnoreCase(User user, String keyword);
}