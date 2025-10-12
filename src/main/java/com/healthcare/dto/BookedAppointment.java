package com.healthcare.dto;

import com.healthcare.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookedAppointment {

	private User user;
	private Long slotId;
	private Long doctorId;
	private Long appointmentId;
	private String doctorFirstName;
	private String doctorLastName;
	private String doctorMobile;
	private String doctorEmail;
	private String specialization;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String slotType;
	private String appointmentStatus;

}
