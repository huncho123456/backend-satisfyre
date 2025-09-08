package com.Satisfyre.app.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.Satisfyre.app.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    // Generic
    private int status;
    private String message;

    // For login
    private String token;
    private UserRole role;
    private Boolean active;
    private String expirationTime;
    private String referredBy;

    // User data
    private String password;
    private UserDTO user;
    private List<UserDTO> users;

    // Downlines
    private String referralCode;
    private List<UserDTO> downlines;

    // Flattened user fields (optional)
    private LocalDate dateOfBirth;
    private String sex;
    private String maritalStatus;
    private String homeAddress;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String employmentStatus;

    private final LocalDateTime timestamp = LocalDateTime.now();
}
