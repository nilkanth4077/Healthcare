package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateDoctorResponse {

    private Long doctorId;
    private String firstName;
    private String lastName;
    private String email;
    private String verificationStatus;
    private String specialization;
    private String mobile;
    private Integer fileSize;

}