package com.Satisfyre.app.controller;


import com.Satisfyre.app.dto.DownlineDTO;
import com.Satisfyre.app.dto.Response;
import com.Satisfyre.app.dto.UserDTO;
import com.Satisfyre.app.entity.UserEntity;
import com.Satisfyre.app.repo.UserRepository;
import com.Satisfyre.app.security.JwtUtils;
import com.Satisfyre.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;


    @GetMapping("/all")
//    @PreAuthorize("hasAuthority('ADMIN')") // ADMIN ALONE HAVE ACCESS TO THIS API
    public ResponseEntity<Response> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/update")
    public ResponseEntity<Response> updateOwnAccount(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Response> deleteOwnAccount() {
        return ResponseEntity.ok(userService.deleteOwnAccount());
    }

    @GetMapping("/account")
    public ResponseEntity<Response> getOwnAccountDetails() {
        return ResponseEntity.ok(userService.getOwnAccountDetails());
    }

    @GetMapping("/downlines/{referralCode}")
    public ResponseEntity<List<UserEntity>> getDownlines(@PathVariable String referralCode) {
        return ResponseEntity.ok(userService.getAllDownlines(referralCode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getUserById(@PathVariable Long id) {
        Response response = userService.getUserById(id);

        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/upline-name/{referralCode}")
    public ResponseEntity<String> getUplineName(@PathVariable String referralCode) {
        String fullName = userService.getUplineNameByReferralCode(referralCode);
        return ResponseEntity.ok(fullName);
    }

    @GetMapping("/downlines-with-levels/{referralCode}")
    public ResponseEntity<List<DownlineDTO>> getDownlinesWithLevels(@PathVariable String referralCode) {
        return ResponseEntity.ok(userService.getAllDownlinesWithLevels(referralCode));
    }

    //By level
    @GetMapping("/downlines/level/{level}/{referralCode}")
    public ResponseEntity<List<UserEntity>> getDownlinesByLevel(
            @PathVariable int level,
            @PathVariable String referralCode
    ) {
        return ResponseEntity.ok(userService.getDownlinesByLevel(referralCode, level));
    }

    @GetMapping("/downlines/grouped/{referralCode}")
    public ResponseEntity<Map<Integer, List<UserEntity>>> getDownlinesGrouped(@PathVariable String referralCode) {
        return ResponseEntity.ok(userService.getDownlinesGroupedByLevel(referralCode));
    }

    // 👉 Update profile picture
    @PutMapping("/{userId}/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = userService.updateProfilePicture(userId, file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            e.printStackTrace(); // log full error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to upload: " + e.getMessage());
        }
    }


    // 👉 Delete profile picture
    @DeleteMapping("/{userId}/profile-picture")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long userId) {
        userService.deleteProfilePicture(userId);
        return ResponseEntity.ok("Profile picture deleted successfully");
    }






}
