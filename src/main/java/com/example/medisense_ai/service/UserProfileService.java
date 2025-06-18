package com.example.medisense_ai.service;

import com.example.medisense_ai.model.User;
import com.example.medisense_ai.model.UserProfile;
import com.example.medisense_ai.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final String uploadDir = "uploads/profile-pictures";

    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserProfile saveUserProfile(UserProfile userProfile) {
        return userProfileRepository.save(userProfile);
    }

    public UserProfile createDefaultProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        return userProfileRepository.save(profile);
    }

    public Optional<UserProfile> findById(Long id) {
        return userProfileRepository.findById(id);
    }

    public Optional<UserProfile> findByUser(User user) {
        return userProfileRepository.findByUser(user);
    }

    public UserProfile getOrCreateProfile(User user) {
        return userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));
    }

    public UserProfile updateProfilePicture(User user, MultipartFile file) throws IOException {
        UserProfile profile = getOrCreateProfile(user);
        
        if (file != null && !file.isEmpty()) {
            // Delete old profile picture if exists
            if (profile.getProfilePicture() != null) {
                try {
                    Files.deleteIfExists(Paths.get(profile.getProfilePicture()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            // Save new profile picture
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath);
            
            profile.setProfilePicture(filePath.toString());
            return userProfileRepository.save(profile);
        }
        
        return profile;
    }

    public void deleteUserProfile(Long id) {
        userProfileRepository.deleteById(id);
    }
}