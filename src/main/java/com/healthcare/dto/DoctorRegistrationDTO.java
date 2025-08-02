package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DoctorRegistrationDTO {

    @Schema(example = "Neel")
    private String firstName;

    @Schema(example = "Patel")
    private String lastName;

    @Schema(example = "neel123@gmail.com")
    private String email;

    @Schema(example = "StrongPassword123!")
    private String password;

    @Schema(example = "StrongPassword123!")
    private String confirmPassword;

    @Schema(example = "9876543210")
    private String mobile;

    @Schema(example = "Cardiologist")
    private String specialization;

}