package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateSlotResponse {

    @Schema(example = "1", description = "Enter slot Id")
    private Long slotId;

    @Schema(example = "2025-08-22T11:00", description = "Enter slot starting time of Slot")
    private LocalDateTime startTime;

    @Schema(example = "2025-08-22T11:00", description = "Enter slot starting time of Slot")
    private LocalDateTime endTime;

    @Schema(example = "ONLINE", description = "Enter slot type")
    private String slotType;

    @Schema(example = "true", description = "Enter slot availability")
    private Boolean available;

    private Long doctorId;
}