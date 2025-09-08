package com.Satisfyre.app.dto;


import com.Satisfyre.app.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.Satisfyre.app.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;
    private String role;
    private boolean active;

    private String referralCode;
    private String referredBy;

    private LocalDate dateOfBirth;
    private String sex;
    private String maritalStatus;
    private String homeAddress;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String employmentStatus;

    private String message;

    private UserDTO user;
    private List<UserDTO> users;       // list of downlines


    public static UserDTO fromEntity(UserEntity user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.isActive())
                .referralCode(user.getReferralCode())
                .referredBy(user.getReferredBy())
                .dateOfBirth(user.getDateOfBirth())
                .sex(user.getSex())
                .maritalStatus(user.getMaritalStatus())
                .homeAddress(user.getHomeAddress())
                .bankName(user.getBankName())
                .accountNumber(user.getAccountNumber())
                .accountName(user.getAccountName())
                .employmentStatus(user.getEmploymentStatus())
                .build();
    }
}


