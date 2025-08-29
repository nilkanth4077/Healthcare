package com.healthcare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SlotResponse {
    private Long id;
    private String slotType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;
}