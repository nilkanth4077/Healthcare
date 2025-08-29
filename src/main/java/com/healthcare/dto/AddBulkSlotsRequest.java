package com.healthcare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AddBulkSlotsRequest {

    @Schema(example = "1", description = "Enter doctor Id")
    private Long doctorId;

    @Schema(example = "ONLINE", description = "Enter the type of mode")
    private String slotType;

    @Schema(example = "2025-08-20", description = "Enter start date")
    private LocalDate startDate;

    @Schema(example = "2025-08-27", description = "Enter end date")
    private LocalDate endDate;

    @Schema(example = "[\"10:00\", \"10:30\", \"14:00\"]", description = "Enter slots in a day")
    private List<LocalTime> times; // Example: ["10:00", "10:30", "14:00"]
}