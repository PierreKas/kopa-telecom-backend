package com.kopa.customeridentity.profile.controller;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.profile.dto.ProfileResponse;
import com.kopa.customeridentity.profile.dto.UpdateProfileRequest;
import com.kopa.customeridentity.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "FR-12 - view/update own profile. Client (mobile app) facing only, authenticated.")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    @Operation(summary = "View own profile")
    public ResponseEntity<ProfileResponse> getProfile(CallerContext caller) {
        return ResponseEntity.ok(profileService.getProfile(caller));
    }

    @PutMapping("/me")
    @Operation(summary = "Update editable profile fields")
    public ResponseEntity<ProfileResponse> updateProfile(CallerContext caller, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(caller, request));
    }
}
