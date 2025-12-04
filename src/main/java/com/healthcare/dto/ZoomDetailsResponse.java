package com.healthcare.dto;

import com.healthcare.entity.Appointment;
import com.healthcare.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ZoomDetailsResponse {

	private Long id;

	private String meetingId;

	private String startUrl;

	private String joinUrl;

	private String password;

	private String topic;

	private String status;

	private Integer type;

	private String startTime;

	private Integer duration;

	private String timezone;

	private Long appointmentId;

}
