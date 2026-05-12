package com.moodsense.api.controller;

import com.moodsense.api.security.CustomUserDetails;
import com.moodsense.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(Authentication authentication, @RequestBody Map<String, String> request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(Authentication authentication, @RequestBody Map<String, String> request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(userService.changePassword(userId, request));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        Long userId = getUserId(authentication);
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
