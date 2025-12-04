package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.dto.ZoomDetailsResponse;
import com.healthcare.entity.ZoomDetails;
import com.healthcare.exception.UserException;
import com.healthcare.service.ZoomMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/zoom")
public class ZoomController {

    @Autowired
    ZoomMeetingService zoomMeetingService;

    @PostMapping("/create-meeting")
    public ResponseEntity<StandardDTO<ZoomDetails>> createMeeting(@RequestParam Long appointmentId) {
        try {
            ZoomDetails meeting = zoomMeetingService.createZoomMeeting(appointmentId);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Meeting created successfully", meeting, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @GetMapping("/meet-details")
    public ResponseEntity<StandardDTO<ZoomDetailsResponse>> getMeetingDetails(@RequestParam Long appointmentId) {
        try {
            ZoomDetailsResponse meeting = zoomMeetingService.getMeetingDetails(appointmentId);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Meeting details fetched successfully", meeting, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }
}