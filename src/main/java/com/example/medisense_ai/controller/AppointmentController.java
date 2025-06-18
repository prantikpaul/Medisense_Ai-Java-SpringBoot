package com.example.medisense_ai.controller;

import com.example.medisense_ai.model.Appointment;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.service.AppointmentService;
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
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    private static final List<String> DEMO_DOCTORS = Arrays.asList(
        "Dr. Alice Smith (Cardiologist)",
        "Dr. Bob Johnson (General Practitioner)",
        "Dr. Carol Lee (Dermatologist)",
        "Dr. David Kim (Pediatrician)"
    );

    @Autowired
    public AppointmentController(UserService userService, AppointmentService appointmentService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String viewAppointments(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Appointment> upcomingAppointments = appointmentService.findUpcomingAppointments(user);
        
        model.addAttribute("user", user);
        model.addAttribute("appointments", upcomingAppointments);
        return "appointments";
    }

    @GetMapping("/add")
    public String addAppointmentForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("doctors", DEMO_DOCTORS);
        return "appointments-add";
    }

    @PostMapping("/add")
    public String addAppointment(@ModelAttribute Appointment appointment, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        appointment.setUser(user);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED); // Default status for new appointments
        
        try {
            appointmentService.saveAppointment(appointment);
            redirectAttributes.addFlashAttribute("success", "Appointment scheduled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to schedule appointment: " + e.getMessage());
        }
        
        return "redirect:/appointments";
    }

    @GetMapping("/history")
    public String viewAppointmentHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Default to last 6 months if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(6);
        }
        if (endDate == null) {
            endDate = LocalDate.now().plusMonths(3); // Include future appointments
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<Appointment> appointments;
        if (status != null && !status.isEmpty()) {
            appointments = appointmentService.findAppointmentsByStatus(user, Appointment.AppointmentStatus.valueOf(status));
        } else {
            appointments = appointmentService.findAppointmentsByDateRange(user, startDateTime, endDateTime);
        }
        
        model.addAttribute("user", user);
        model.addAttribute("appointments", appointments);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedStatus", status);
        return "appointments-history";
    }

    @GetMapping("/edit/{id}")
    public String editAppointmentForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Ensure the appointment belongs to the current user
        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("appointment", appointment);
        model.addAttribute("doctors", DEMO_DOCTORS);
        return "appointments-edit";
    }

    @PostMapping("/update/{id}")
    public String updateAppointment(@PathVariable Long id, @ModelAttribute Appointment appointment, 
                                   RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        Appointment existingAppointment = appointmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Ensure the appointment belongs to the current user
        if (!existingAppointment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }
        
        // Update fields but preserve user
        appointment.setId(existingAppointment.getId());
        appointment.setUser(existingAppointment.getUser());
        
        try {
            appointmentService.saveAppointment(appointment);
            redirectAttributes.addFlashAttribute("success", "Appointment updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update appointment: " + e.getMessage());
        }
        
        return "redirect:/appointments";
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Ensure the appointment belongs to the current user
        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }
        
        try {
            appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel appointment: " + e.getMessage());
        }
        
        return "redirect:/appointments";
    }

    @PostMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Ensure the appointment belongs to the current user
        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }
        
        try {
            appointmentService.deleteAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete appointment: " + e.getMessage());
        }
        
        return "redirect:/appointments";
    }
}