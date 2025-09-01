package com.healthcare.controller;

import com.healthcare.dto.*;
import com.healthcare.entity.DoctorSlot;
import com.healthcare.exception.UserException;
import com.healthcare.repository.DoctorSlotRepository;
import com.healthcare.service.DoctorSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/slot")
@RequiredArgsConstructor
public class DoctorSlotController {

    private final DoctorSlotService slotService;
    private final DoctorSlotRepository slotRepository;

    @PostMapping("/add")
    public ResponseEntity<StandardDTO<Map<String, Object>>> addBulkSlots(@RequestBody AddBulkSlotsRequest request, @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Map<String, Object> response = slotService.addSlots(
                    request.getDoctorId(),
                    request.getSlotType(),
                    request.getStartDate(),
                    request.getEndDate(),
                    actualToken,
                    request.getTimes().toArray(new LocalTime[0])
            );
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("extraMessage", "You can only create slots from now until the allowed end time");
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Slots added successfully", response, metadata)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @PutMapping("/update")
    public ResponseEntity<StandardDTO<UpdateSlotResponse>> updateSlot(@RequestBody UpdateSlotRequest request, @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            UpdateSlotResponse response = slotService.updateSlot(request.getSlotId(), request.getNewStartTime(), request.getSlotType(), request.getAvailable(), actualToken);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Slot updated successfully", response, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<StandardDTO<Map<String, Object>>> deleteSlot(@RequestParam Long slotId, @RequestHeader("Authorization") String token) throws UserException {
        try {
            String actualToken = token.replace("Bearer ", "");

            slotService.deleteSlot(slotId, actualToken);

            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(),
                            "Slot deleted successfully",
                            Map.of("slotId", slotId),
                            null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @GetMapping("/get/{slotId}")
    public ResponseEntity<StandardDTO<Map<String, Object>>> getSlotById(@PathVariable Long slotId) {
        return ResponseEntity.ok(slotService.getSlotWithDoctor(slotId));
    }

    @GetMapping("/doctor/{doctorId}")
    public StandardDTO<List<SlotResponse>> getSlotsByDoctor(@PathVariable Long doctorId) {
        List<SlotResponse> slots = slotRepository.findAll()
                .stream()
                .filter(s -> s.getDoctor().getId().equals(doctorId))
                .map(s -> {
                    SlotResponse resp = new SlotResponse();
                    resp.setId(s.getId());
                    resp.setSlotType(s.getSlotType());
                    resp.setStartTime(s.getStartTime());
                    resp.setEndTime(s.getEndTime());
                    resp.setAvailable(s.isAvailable());
                    return resp;
                })
                .collect(Collectors.toList());

        Map<String, Object> metaData = new HashMap<>();
        metaData.put("fetchedSlots", slots.size());

        return new StandardDTO<>(HttpStatus.OK.value(), "Doctor slots fetched successfully", slots, metaData);
    }
}