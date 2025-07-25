package com.e_commerce.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;

    private Long actorId;
    private String actorRole;
    private String targetEntity;
    private Long targetId;
    private String action;
    private String message;
    private String ipAddress;
    private LocalDateTime timestamp = LocalDateTime.now();
}