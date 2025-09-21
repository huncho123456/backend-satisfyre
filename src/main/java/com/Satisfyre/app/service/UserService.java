package com.Satisfyre.app.service;


import com.Satisfyre.app.dto.*;
import com.Satisfyre.app.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    Response registerUser(RegistrationRequest registrationRequest);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    Response getOwnAccountDetails();
    UserEntity getCurrentLoggedInUser();
    Response updateOwnAccount(UserDTO userDTO);
    Response deleteOwnAccount();
    List<UserEntity> getDirectDownlines(String referralCode);
    List<UserEntity> getAllDownlines(String referralCode);
    Response getUserById (Long id);
    String getUplineNameByReferralCode(String referralCode);
    List<DownlineDTO> getAllDownlinesWithLevels(String referralCode);
    List<UserEntity> getDownlinesByLevel(String referralCode, int targetLevel);
    Map<Integer, List<UserEntity>> getDownlinesGroupedByLevel(String referralCode);
    Response requestPasswordReset(String email);
    Response resetPassword(String token, String newPassword);
    String updateProfilePicture(Long userId, MultipartFile file);
    void deleteProfilePicture(Long userId);




}
