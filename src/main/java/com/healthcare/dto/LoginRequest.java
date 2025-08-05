package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(example = "nilkanth4077@gmail.com")
    private String email;

    @Schema(example = "Nilkanth@4077")
    private String password;

    public LoginRequest() {
        super();
    }

}
