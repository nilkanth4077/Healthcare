package com.healthcare.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Cardiologist", description = "Your specialization")
    @Column(name = "specialization")
    private String specialization;

    @Schema(example = "Pending", description = "Verification status")
    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "doc_document", columnDefinition = "BYTEA")
    private byte[] document;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
