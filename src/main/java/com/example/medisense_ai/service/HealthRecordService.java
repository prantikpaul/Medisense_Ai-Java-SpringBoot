package com.example.medisense_ai.service;

import com.example.medisense_ai.model.HealthRecord;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.repository.HealthRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final String uploadDir = "uploads/health-records";

    @Autowired
    public HealthRecordService(HealthRecordRepository healthRecordRepository) {
        this.healthRecordRepository = healthRecordRepository;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HealthRecord saveHealthRecord(HealthRecord healthRecord, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath);
            
            healthRecord.setDocumentPath(filePath.toString());
            healthRecord.setDocumentName(file.getOriginalFilename());
            healthRecord.setDocumentType(file.getContentType());
            healthRecord.setDocumentSize(file.getSize());
        }
        
        return healthRecordRepository.save(healthRecord);
    }

    public HealthRecord saveHealthRecord(HealthRecord healthRecord) {
        return healthRecordRepository.save(healthRecord);
    }

    public Optional<HealthRecord> findById(Long id) {
        return healthRecordRepository.findById(id);
    }

    public List<HealthRecord> findAllByUser(User user) {
        return healthRecordRepository.findByUser(user);
    }

    public List<HealthRecord> findRecentRecordsByUser(User user) {
        return healthRecordRepository.findByUserOrderByUploadedAtDesc(user);
    }

    public List<HealthRecord> findRecordsByType(User user, String recordType) {
        return healthRecordRepository.findByUserAndRecordType(user, recordType);
    }

    public List<HealthRecord> findRecordsByDateRange(User user, LocalDate start, LocalDate end) {
        return healthRecordRepository.findByUserAndRecordDateBetween(user, start, end);
    }

    public List<HealthRecord> searchRecords(User user, String keyword) {
        return healthRecordRepository.findByUserAndTitleContainingIgnoreCase(user, keyword);
    }

    public void deleteHealthRecord(Long id) {
        Optional<HealthRecord> recordOpt = healthRecordRepository.findById(id);
        if (recordOpt.isPresent()) {
            HealthRecord record = recordOpt.get();
            // Delete the file if it exists
            if (record.getDocumentPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(record.getDocumentPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            healthRecordRepository.deleteById(id);
        }
    }
}