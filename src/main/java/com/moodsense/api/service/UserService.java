package com.moodsense.api.service;

import com.moodsense.api.model.User;
import com.moodsense.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getProfile(Long userId) {
        User user = getUser(userId);
        return Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt()
        );
    }

    public Map<String, Object> updateProfile(Long userId, Map<String, String> request) {
        User user = getUser(userId);
        
        String newFullName = request.get("fullName");
        if (newFullName == null || newFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        
        user.setFullName(newFullName.trim());
        userRepository.save(user);
        
        return getProfile(userId);
    }

    public Map<String, String> changePassword(Long userId, Map<String, String> request) {
        User user = getUser(userId);
        
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmNewPassword = request.get("confirmNewPassword");
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return Map.of("message", "Password changed successfully");
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = getUser(userId);
        userRepository.delete(user);
        log.info("User {} deleted their account", userId);
    }
    
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
