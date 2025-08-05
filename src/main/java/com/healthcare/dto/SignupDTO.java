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

    @Schema(example = "Neel")
    private String firstName;

    @Schema(example = "Patel")
    private String lastName;

    @Schema(example = "neel123@gmail.com")
    private String email;

    @Schema(example = "P@ssw0rd123", description = "Enter your password")
    private String password;

    @Schema(example = "P@ssw0rd123", description = "Confirm your password")
    private String confirmPassword;

    @Schema(example = "USER")
    private String role;

    @Schema(example = "9876543210")
    private String mobile;

}
