package com.healthcare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "zoom-details")
public class ZoomDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meetingId;

    @Column(columnDefinition = "TEXT")
    private String startUrl;

    @Column(columnDefinition = "TEXT")
    private String joinUrl;

    private String password;

    private String hostEmail;

    private String topic;

    private String status;

    private Integer type;

    private String startTime;
    private Integer duration;

    private String timezone;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;
}