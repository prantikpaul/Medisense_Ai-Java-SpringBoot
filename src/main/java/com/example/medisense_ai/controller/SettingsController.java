package com.example.medisense_ai.controller;

import com.example.medisense_ai.model.User;
import com.example.medisense_ai.model.UserProfile;
import com.example.medisense_ai.service.UserProfileService;
import com.example.medisense_ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SettingsController(UserService userService, UserProfileService userProfileService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String viewSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "settings";
    }

    @GetMapping("/account")
    public String viewAccountSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        return "settings-account";
    }

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam String email, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            // Check if email is already in use
            if (userService.findByEmail(email).isPresent() && !user.getEmail().equals(email)) {
                redirectAttributes.addFlashAttribute("error", "Email is already in use");
                return "redirect:/settings/account";
            }
            
            user.setEmail(email);
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Email updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update email: " + e.getMessage());
        }
        
        return "redirect:/settings/account";
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String currentPassword, 
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/settings/account";
        }
        
        // Validate new password
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/settings/account";
        }
        
        try {
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Password updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update password: " + e.getMessage());
        }
        
        return "redirect:/settings/account";
    }

    @GetMapping("/notifications")
    public String viewNotificationSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "settings-notifications";
    }

    @PostMapping("/update-notifications")
    public String updateNotificationSettings(
            @RequestParam(required = false, defaultValue = "false") boolean emailNotifications,
            @RequestParam(required = false, defaultValue = "false") boolean smsNotifications,
            @RequestParam(required = false, defaultValue = "false") boolean appointmentReminders,
            @RequestParam(required = false, defaultValue = "false") boolean medicationReminders,
            @RequestParam(required = false, defaultValue = "false") boolean healthTips,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        try {
            // Update notification preferences
            profile.setEmailNotifications(emailNotifications);
            profile.setSmsNotifications(smsNotifications);
            profile.setAppointmentReminders(appointmentReminders);
            profile.setMedicationReminders(medicationReminders);
            profile.setHealthTips(healthTips);
            
            userProfileService.saveUserProfile(profile);
            redirectAttributes.addFlashAttribute("success", "Notification settings updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update notification settings: " + e.getMessage());
        }
        
        return "redirect:/settings/notifications";
    }

    @GetMapping("/privacy")
    public String viewPrivacySettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "settings-privacy";
    }

    @PostMapping("/update-privacy")
    public String updatePrivacySettings(
            @RequestParam(required = false, defaultValue = "false") boolean shareHealthData,
            @RequestParam(required = false, defaultValue = "false") boolean shareActivityData,
            @RequestParam(required = false, defaultValue = "false") boolean allowDataAnalysis,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        try {
            // Update privacy preferences
            profile.setShareHealthData(shareHealthData);
            profile.setShareActivityData(shareActivityData);
            profile.setAllowDataAnalysis(allowDataAnalysis);
            
            userProfileService.saveUserProfile(profile);
            redirectAttributes.addFlashAttribute("success", "Privacy settings updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update privacy settings: " + e.getMessage());
        }
        
        return "redirect:/settings/privacy";
    }

    @GetMapping("/delete-account")
    public String viewDeleteAccount(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        return "settings-delete-account";
    }

    @PostMapping("/confirm-delete-account")
    public String confirmDeleteAccount(@RequestParam String password, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password is incorrect");
            return "redirect:/settings/delete-account";
        }
        
        try {
            // Delete user profile first (this will handle associated files)
            userProfileService.deleteUserProfile(user.getId());
            
            // Delete user account
            userService.deleteUser(user.getId());
            
            // Logout user
            SecurityContextHolder.clearContext();
            
            redirectAttributes.addFlashAttribute("success", "Your account has been deleted successfully");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
            return "redirect:/settings/delete-account";
        }
    }
}