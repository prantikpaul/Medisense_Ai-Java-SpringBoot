package com.example.medisense_ai.controller;

import com.example.medisense_ai.model.HealthRecord;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.service.HealthRecordService;
import com.example.medisense_ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/health-records")
public class HealthRecordController {

    private final UserService userService;
    private final HealthRecordService healthRecordService;

    @Autowired
    public HealthRecordController(UserService userService, HealthRecordService healthRecordService) {
        this.userService = userService;
        this.healthRecordService = healthRecordService;
    }

    @GetMapping
    public String viewHealthRecords(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<HealthRecord> recentRecords = healthRecordService.findRecentRecordsByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("records", recentRecords);
        return "health-records";
    }

    @GetMapping("/add")
    public String addHealthRecordForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("healthRecord", new HealthRecord());
        return "health-records-add";
    }

    @PostMapping("/add")
    public String addHealthRecord(@ModelAttribute HealthRecord healthRecord,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        healthRecord.setUser(user);
        healthRecord.setUploadDate(LocalDateTime.now());
        
        try {
            if (!file.isEmpty()) {
                healthRecordService.saveHealthRecordWithFile(healthRecord, file);
            } else {
                healthRecordService.saveHealthRecord(healthRecord);
            }
            redirectAttributes.addFlashAttribute("success", "Health record added successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add health record: " + e.getMessage());
        }
        
        return "redirect:/health-records";
    }

    @GetMapping("/search")
    public String searchHealthRecords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<HealthRecord> records;
        
        // Default to last 12 months if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(12);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        if (keyword != null && !keyword.isEmpty()) {
            records = healthRecordService.searchRecordsByKeyword(user, keyword);
        } else if (recordType != null && !recordType.isEmpty()) {
            records = healthRecordService.findRecordsByType(user, recordType);
        } else {
            records = healthRecordService.findRecordsByDateRange(user, startDateTime, endDateTime);
        }
        
        model.addAttribute("user", user);
        model.addAttribute("records", records);
        model.addAttribute("keyword", keyword);
        model.addAttribute("recordType", recordType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "health-records-search";
    }

    @GetMapping("/view/{id}")
    public String viewHealthRecord(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthRecord healthRecord = healthRecordService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health record not found"));
        
        // Ensure the record belongs to the current user
        if (!healthRecord.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health record");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("record", healthRecord);
        return "health-records-view";
    }

    @GetMapping("/edit/{id}")
    public String editHealthRecordForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthRecord healthRecord = healthRecordService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health record not found"));
        
        // Ensure the record belongs to the current user
        if (!healthRecord.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health record");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("healthRecord", healthRecord);
        return "health-records-edit";
    }

    @PostMapping("/update/{id}")
    public String updateHealthRecord(@PathVariable Long id, 
                                    @ModelAttribute HealthRecord healthRecord,
                                    @RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthRecord existingRecord = healthRecordService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health record not found"));
        
        // Ensure the record belongs to the current user
        if (!existingRecord.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health record");
        }
        
        // Update fields but preserve user and upload date
        healthRecord.setId(existingRecord.getId());
        healthRecord.setUser(existingRecord.getUser());
        healthRecord.setUploadDate(existingRecord.getUploadDate());
        healthRecord.setFilePath(existingRecord.getFilePath()); // Preserve existing file path unless new file uploaded
        
        try {
            if (!file.isEmpty()) {
                healthRecordService.saveHealthRecordWithFile(healthRecord, file);
            } else {
                healthRecordService.saveHealthRecord(healthRecord);
            }
            redirectAttributes.addFlashAttribute("success", "Health record updated successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update health record: " + e.getMessage());
        }
        
        return "redirect:/health-records";
    }

    @PostMapping("/delete/{id}")
    public String deleteHealthRecord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthRecord healthRecord = healthRecordService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health record not found"));
        
        // Ensure the record belongs to the current user
        if (!healthRecord.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health record");
        }
        
        try {
            healthRecordService.deleteHealthRecord(id);
            redirectAttributes.addFlashAttribute("success", "Health record deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete health record: " + e.getMessage());
        }
        
        return "redirect:/health-records";
    }
}