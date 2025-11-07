package com.Satisfyre.app.service.impl;

import com.Satisfyre.app.config.dotenvConfig;
import com.Satisfyre.app.dto.*;
import com.Satisfyre.app.entity.PasswordResetToken;
import com.Satisfyre.app.exceptions.InvalidCredentialException;
import com.Satisfyre.app.exceptions.NotFoundException;
import com.Satisfyre.app.notification.NotificationService;
import com.Satisfyre.app.repo.PasswordResetTokenRepository;
import com.Satisfyre.app.service.UserService;
import com.Satisfyre.app.entity.UserEntity;
import com.Satisfyre.app.enums.UserRole;
import com.Satisfyre.app.repo.UserRepository;
import com.Satisfyre.app.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    String BASEURL = dotenvConfig.get("BASEURL");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final PasswordResetTokenRepository tokenRepository;
    private final CloudinaryService cloudinaryService;
    String FRONT_ENDPOINT = dotenvConfig.get("FRONTEND_BASEURL");


    @Override
    public Response registerUser(RegistrationRequest registrationRequest) {
        log.info("INSIDE registerUser()");

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registrationRequest.getEmail());
        }

        UserRole role = registrationRequest.getRole() != null ? registrationRequest.getRole() : UserRole.CUSTOMER;

        // Hash the password for storage
        String hashedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        UserEntity userToSave = UserEntity.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(hashedPassword)
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .active(true)
                .dateOfBirth(registrationRequest.getDateOfBirth())
                .sex(registrationRequest.getSex())
                .maritalStatus(registrationRequest.getMaritalStatus())
                .homeAddress(registrationRequest.getHomeAddress())
                .bankName(registrationRequest.getBankName())
                .accountNumber(registrationRequest.getAccountNumber())
                .accountName(registrationRequest.getAccountName())
                .employmentStatus(registrationRequest.getEmploymentStatus())
                .build();

        // Generate and set referral code
        userToSave.setReferralCode(generateReferralCode(registrationRequest.getFirstName()));

        // Optionally set the upline referral if present and valid
        if (registrationRequest.getReferredBy() != null) {
            Optional<UserEntity> upline = userRepository.findByReferralCode(registrationRequest.getReferredBy());
            upline.ifPresent(user -> userToSave.setReferredBy(user.getReferralCode()));

        }

        // Save user
        UserEntity saved = userRepository.save(userToSave);

        UserDTO userDTO = modelMapper.map(saved, UserDTO.class);

        notificationService.sendWelcomeEmail(
                saved.getEmail(),
                saved.getFirstName() + " " + saved.getLastName(),
                saved.getEmail(),
                registrationRequest.getPassword(),
                saved.getPhoneNumber(),
                saved.getReferralCode()

        );

        return Response.builder()
                .status(200)
                .referralCode(saved.getReferralCode())
                .user(userDTO)
                .message(saved.getEmail() + " Registered successfully. Check your email.")
                .build();
    }


    public String getUplineNameByReferralCode(String referralCode) {
        return userRepository.findByReferralCode(referralCode)
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElseThrow(() -> new IllegalArgumentException("Referral code not found: " + referralCode));
    }


    @Override
    public Response loginUser(LoginRequest loginRequest) {

        log.info("INSIDE loginUser() " + loginRequest.getEmail());

        String JWTExpires = "30 days";

        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email Not Found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Password does not match");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .status(200)
                .message("user logged in successfully")
                .role(user.getRole())
                .token(token)
                .active(user.isActive())
                .expirationTime(JWTExpires)
                .build();

    }

    @Override
    public Response getAllUsers() {

        log.info("INSIDE getAllUsers()");

        List<UserEntity> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOList = modelMapper.map(users, new TypeToken<List<UserDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOList)
                .build();

    }

    @Override
    public Response getOwnAccountDetails() {

        log.info("INSIDE getOwnAccountDetails()");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user not found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }

    @Override
    public UserEntity getCurrentLoggedInUser() {

        log.info("INSIDE getCurrentLoggedInUser()");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @Override
    public Response updateOwnAccount(UserDTO userDTO) {

        log.info("INSIDE updateOwnAccount()");
        UserEntity existingUser = getCurrentLoggedInUser();

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getFirstName() != null) existingUser.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) existingUser.setLastName(userDTO.getLastName());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getAccountName() != null) existingUser.setAccountName(userDTO.getAccountName());
        if (userDTO.getAccountNumber() != null) existingUser.setAccountNumber(userDTO.getAccountNumber());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());


        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("User Updated Successfully")
                .build();

    }

    @Override
    public Response deleteOwnAccount() {

        log.info("INSIDE deleteOwnAccount()");

        UserEntity currentUser = getCurrentLoggedInUser();

        userRepository.delete(currentUser);

        return Response.builder()
                .status(200)
                .message("User Deleted Successfully")
                .build();
    }

    private String generateReferralCode(String name) {
        return name.substring(0, 3).toUpperCase() + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    @Override
    public String updateProfilePicture(Long userId, MultipartFile file) {

        log.info("Update user pic");
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Optional: delete old image before uploading new one
        if (user.getProfilePicUrl() != null) {
            String publicId = extractPublicId(user.getProfilePicUrl());
            cloudinaryService.deleteFile(publicId);
        }

        // Upload new image
        String imageUrl = cloudinaryService.uploadFile(file);
        System.out.println(imageUrl);
        user.setProfilePicUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    private String extractPublicId(String imageUrl) {
        // ⚡ Cloudinary URLs look like: https://res.cloudinary.com/demo/image/upload/v1234567890/profile_pics/abcd1234.jpg
        // You need to extract "profile_pics/abcd1234"
        String[] parts = imageUrl.split("/");
        String fileName = parts[parts.length - 1];
        return "profile_pics/" + fileName.split("\\.")[0];
    }

    @Override
    public void deleteProfilePicture(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfilePicUrl() != null) {
            String publicId = extractPublicId(user.getProfilePicUrl());
            cloudinaryService.deleteFile(publicId);

            // remove link from DB
            user.setProfilePicUrl(null);
            userRepository.save(user);

        }
    }




    @Override
    public Response requestPasswordReset(String email) {

        log.info("User RequestPasswordReset() " + email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PasswordResetToken token = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(token);

        String resetLink = FRONT_ENDPOINT + "/html/auth-reset-password-basic.html?token=" + token.getToken();

        notificationService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                resetLink
        );

        return Response.builder()
                .status(200)
                .message("Password reset link sent to email")
                .build();
    }

    @Override
    public Response resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new NotFoundException("Invalid token"));
                log.info(" Found in DB: {}", resetToken.getToken());

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialException("Token expired or already used");
        }

        UserEntity user = resetToken.getUser();
        log.info("User PasswordReset" + user.getEmail());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return Response.builder()
                .status(200)
                .message("Password reset successful")
                .build();
    }


    @Override
    public List<UserEntity> getDirectDownlines(String referralCode) {
        return userRepository.findAllByReferredBy(referralCode);
    }

    @Override
    public List<UserEntity> getAllDownlines(String referralCode) {
        List<UserEntity> all = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(referralCode);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<UserEntity> downlines = userRepository.findAllByReferredBy(current);
            all.addAll(downlines);
            downlines.forEach(u -> queue.add(u.getReferralCode()));
        }

        return all;
    }

    @Override
    public List<DownlineDTO> getAllDownlinesWithLevels(String referralCode) {
        List<DownlineDTO> all = new ArrayList<>();
        Queue<Map.Entry<String, Integer>> queue = new LinkedList<>();

        queue.add(Map.entry(referralCode, 0)); // Start with upline at level 0

        while (!queue.isEmpty()) {
            Map.Entry<String, Integer> current = queue.poll();
            String currentReferralCode = current.getKey();
            int currentLevel = current.getValue();

            List<UserEntity> downlines = userRepository.findAllByReferredBy(currentReferralCode);

            for (UserEntity user : downlines) {
                int newLevel = currentLevel + 1;
                all.add(new DownlineDTO(user, newLevel));
                queue.add(Map.entry(user.getReferralCode(), newLevel));
            }
        }

        return all;
    }

    //  Get only a specific level's downlines
    @Override
    public List<UserEntity> getDownlinesByLevel(String referralCode, int targetLevel) {
        return getAllDownlinesWithLevels(referralCode).stream()
                .filter(dto -> dto.getLevel() == targetLevel)
                .map(DownlineDTO::getUser)
                .toList();
    }

    // Group downlines by level
    @Override
    public Map<Integer, List<UserEntity>> getDownlinesGroupedByLevel(String referralCode) {
        return getAllDownlinesWithLevels(referralCode).stream()
                .collect(Collectors.groupingBy(
                        DownlineDTO::getLevel,
                        Collectors.mapping(DownlineDTO::getUser, Collectors.toList())
                ));
    }

    @Override
    public Response getUserById(Long id) {
        Optional<UserEntity> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            UserDTO userDTO = UserDTO.fromEntity(user);

            // Find downlines
            List<UserEntity> downlineEntities = userRepository.findAllByReferredBy(user.getReferralCode());
            List<UserDTO> downlines = downlineEntities.stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());

            return Response.builder()
                    .status(200)
                    .message("User retrieved successfully")
                    .user(userDTO)        // main user
                    .downlines(downlines)     // downlines
                    .referralCode(user.getReferralCode()) // optional: if you added this field in Response
                    .build();
        } else {
            return Response.builder()
                    .status(404)
                    .message("User not found")
                    .build();
        }
    }





}
