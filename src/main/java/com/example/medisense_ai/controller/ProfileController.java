package com.example.medisense_ai.controller;

import com.example.medisense_ai.model.User;
import com.example.medisense_ai.model.UserProfile;
import com.example.medisense_ai.service.UserProfileService;
import com.example.medisense_ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @Autowired
    public ProfileController(UserService userService, UserProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileService.getOrCreateProfile(user);
        
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "profile-edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserProfile profile,
                               @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                               @RequestParam(value = "allergies", required = false) String allergies,
                               @RequestParam(value = "medications", required = false) String medications,
                               @RequestParam(value = "medicalConditions", required = false) String medicalConditions,
                               @RequestParam(value = "foodPreferences", required = false) String foodPreferences,
                               @RequestParam(value = "foodDislikes", required = false) String foodDislikes,
                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile existingProfile = userProfileService.getOrCreateProfile(user);
        // Update profile fields
        existingProfile.setPhoneNumber(profile.getPhoneNumber());
        existingProfile.setDateOfBirth(profile.getDateOfBirth());
        existingProfile.setWeight(profile.getWeight());
        existingProfile.setBloodType(profile.getBloodType());
        existingProfile.setEmergencyContactName(profile.getEmergencyContactName());
        existingProfile.setEmergencyContactPhone(profile.getEmergencyContactPhone());
        existingProfile.setEmergencyContactRelationship(profile.getEmergencyContactRelationship());
        existingProfile.setFitnessGoals(profile.getFitnessGoals());
        existingProfile.setDietaryPreferences(profile.getDietaryPreferences());
        // Convert comma-separated strings to lists
        existingProfile.setAllergies(stringToList(allergies));
        existingProfile.setMedications(stringToList(medications));
        existingProfile.setMedicalConditions(stringToList(medicalConditions));
        existingProfile.setFoodPreferences(stringToList(foodPreferences));
        existingProfile.setFoodDislikes(stringToList(foodDislikes));
        try {
            // Update profile picture if provided
            if (profileImage != null && !profileImage.isEmpty()) {
                userProfileService.updateProfilePicture(user, profileImage);
            }
            userProfileService.saveUserProfile(existingProfile);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload profile picture: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    private List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) return Collections.emptyList();
        return Arrays.asList(str.split("\s*,\s*"));
    }
}