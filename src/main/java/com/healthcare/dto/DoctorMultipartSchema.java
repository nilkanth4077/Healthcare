package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class DoctorMultipartSchema {

    @Schema(type = "string", format = "binary", description = "Document file")
    public MultipartFile documentFile;

}
