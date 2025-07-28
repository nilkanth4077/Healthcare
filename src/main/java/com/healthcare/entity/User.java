package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Neel", description = "First name of the user")
    private String firstName;

    @Schema(example = "Patel", description = "Last name of the user")
    private String lastName;

    @Schema(example = "neel.patel@example.com", description = "Email address")
    private String email;

    @Schema(example = "P@ssw0rd123", description = "Enter your password")
    private String password;

    @Schema(example = "9876543210", description = "Mobile number")
    private String mobile;

    @Schema(example = "DOCTOR", description = "Role of the user")
    private String role;

}
