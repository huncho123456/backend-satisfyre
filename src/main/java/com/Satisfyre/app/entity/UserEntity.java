package com.Satisfyre.app.entity;

import com.Satisfyre.app.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "First name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(unique = true)
    private String referralCode;

    @Column(name = "referred_by")
    private String referredBy;

    @Column(name = "active")
    private boolean active ;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(name = "sex")
    private String sex;

    @Column(name = "martial_status")
    private String maritalStatus;

    @Column(name = "home_address")
    private String homeAddress;

    @Column(name = "bank_name")
    private String bankName;

    @NotBlank(message = "Account name is required")
    @Column(name = "account_number")
    private String accountNumber;

    @NotBlank(message = "Account name is required")
    @Column(name = "account_name")
    private String accountName;

    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(updatable = false)
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

}
