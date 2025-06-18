package com.example.medisense_ai.controller;

import com.example.medisense_ai.model.HealthMetric;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.service.HealthMetricService;
import com.example.medisense_ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/health-metrics")
public class HealthMetricsController {

    private final UserService userService;
    private final HealthMetricService healthMetricService;

    @Autowired
    public HealthMetricsController(UserService userService, HealthMetricService healthMetricService) {
        this.userService = userService;
        this.healthMetricService = healthMetricService;
    }

    @GetMapping
    public String viewHealthMetrics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<HealthMetric> recentMetrics = healthMetricService.findRecentMetricsByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("metrics", recentMetrics);
        model.addAttribute("newMetric", new HealthMetric());
        return "health-metrics";
    }

    @GetMapping("/add")
    public String addHealthMetricForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("healthMetric", new HealthMetric());
        return "health-metrics-add";
    }

    @PostMapping("/add")
    public String addHealthMetric(@ModelAttribute HealthMetric healthMetric, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        healthMetric.setUser(user);
        healthMetric.setRecordedAt(LocalDateTime.now());
        
        try {
            healthMetricService.saveHealthMetric(healthMetric);
            redirectAttributes.addFlashAttribute("success", "Health metric added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add health metric: " + e.getMessage());
        }
        
        return "redirect:/health-metrics";
    }

    @GetMapping("/history")
    public String viewHealthMetricsHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Default to last 30 days if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<HealthMetric> metrics = healthMetricService.findMetricsByDateRange(user, startDateTime, endDateTime);
        
        model.addAttribute("user", user);
        model.addAttribute("metrics", metrics);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "health-metrics-history";
    }

    @GetMapping("/edit/{id}")
    public String editHealthMetricForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthMetric healthMetric = healthMetricService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health metric not found"));
        
        // Ensure the metric belongs to the current user
        if (!healthMetric.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health metric");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("healthMetric", healthMetric);
        return "health-metrics-edit";
    }

    @PostMapping("/update/{id}")
    public String updateHealthMetric(@PathVariable Long id, @ModelAttribute HealthMetric healthMetric, 
                                    RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthMetric existingMetric = healthMetricService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health metric not found"));
        
        // Ensure the metric belongs to the current user
        if (!existingMetric.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health metric");
        }
        
        // Update fields but preserve user and recorded time
        healthMetric.setId(existingMetric.getId());
        healthMetric.setUser(existingMetric.getUser());
        healthMetric.setRecordedAt(existingMetric.getRecordedAt());
        
        try {
            healthMetricService.saveHealthMetric(healthMetric);
            redirectAttributes.addFlashAttribute("success", "Health metric updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update health metric: " + e.getMessage());
        }
        
        return "redirect:/health-metrics";
    }

    @PostMapping("/delete/{id}")
    public String deleteHealthMetric(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthMetric healthMetric = healthMetricService.findById(id)
                .orElseThrow(() -> new RuntimeException("Health metric not found"));
        
        // Ensure the metric belongs to the current user
        if (!healthMetric.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to health metric");
        }
        
        try {
            healthMetricService.deleteHealthMetric(id);
            redirectAttributes.addFlashAttribute("success", "Health metric deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete health metric: " + e.getMessage());
        }
        
        return "redirect:/health-metrics";
    }
}