package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupDTO {

    @Schema(example = "John")
    private String firstName;

    @Schema(example = "Doe")
    private String lastName;

    @Schema(example = "john.doe@example.com")
    private String email;

    @Schema(example = "P@ssw0rd123", description = "Enter your password")
    private String password;

    @Schema(example = "P@ssw0rd123", description = "Confirm your password")
    private String confirmPassword;

    @Schema(example = "PATIENT")
    private String role;

    @Schema(example = "9876543210")
    private String mobile;

}
