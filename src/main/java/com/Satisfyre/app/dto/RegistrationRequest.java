package com.Satisfyre.app.dto;

import com.Satisfyre.app.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "FirstName is required")
    private String firstName;

    @NotBlank(message = "LastName is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "LastName is required")
    private String phoneNumber;

    private UserRole role; //optional

    private String referredBy;

    @NotBlank(message = "Password is required")
    private String password;

    private LocalDate dateOfBirth;
    private String sex;
    private String maritalStatus;
    private String homeAddress;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String employmentStatus;

}
