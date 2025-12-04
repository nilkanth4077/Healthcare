package com.healthcare.service;

import com.healthcare.dto.ZoomDetailsResponse;
import com.healthcare.entity.Appointment;
import com.healthcare.entity.ZoomDetails;
import com.healthcare.repository.AppointmentRepo;
import com.healthcare.repository.ZoomDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ZoomMeetingService {

    @Autowired
    private ZoomAuthService zoomAuthService;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private ZoomDetailsRepo zoomDetailsRepo;

    @Value("${zoom.base.url}")
    private String zoomBaseUrl;

    public ZoomDetails createZoomMeeting(Long appointmentId) {

        try {
            Appointment appointment = appointmentRepo.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            String token = zoomAuthService.generateAccessToken();
            String email = "nilkanth4077@gmail.com";
            String url = zoomBaseUrl + "/users/" + email + "/meetings";

            RestTemplate rest = new RestTemplate();

            Map<String, Object> body = new HashMap<>();
            body.put("topic", "HB - Consultation");
            body.put("type", 2);
            body.put("duration", 120);
            body.put("password", "DOC123");

            Map<String, Object> settings = new HashMap<>();
            settings.put("waiting_room", false);
            settings.put("join_before_host", true);
            body.put("settings", settings);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            Map<String, Object> response = rest.postForObject(url, request, Map.class);

            ZoomDetails zoomDetails = ZoomDetails.builder()
                    .meetingId(String.valueOf(response.get("id")))
                    .startUrl((String) response.get("start_url"))
                    .joinUrl((String) response.get("join_url"))
                    .password((String) response.get("password"))
                    .topic((String) response.get("topic"))
                    .type((Integer) response.get("type"))
                    .status((String) response.get("status"))
                    .startTime((String) response.get("start_time"))
                    .duration((Integer) response.get("duration"))
                    .timezone((String) response.get("timezone"))
                    .hostEmail(email)
                    .appointment(appointment)
                    .build();

            zoomDetailsRepo.save(zoomDetails);

            return zoomDetails;
        } catch (Exception e) {
            throw new RuntimeException("Meeting already exist for this appointment");
        }
    }

    public ZoomDetailsResponse getMeetingDetails(Long appointmentId) {
        ZoomDetails meeting = zoomDetailsRepo.findByAppointmentId(appointmentId);

        ZoomDetailsResponse res = new ZoomDetailsResponse();
        res.setId(meeting.getId());
        res.setMeetingId(meeting.getMeetingId());
        res.setStartUrl(meeting.getStartUrl());
        res.setJoinUrl(meeting.getJoinUrl());
        res.setPassword(meeting.getPassword());
        res.setTopic(meeting.getTopic());
        res.setStatus(meeting.getStatus());
        res.setStartTime(meeting.getStartTime());
        res.setDuration(meeting.getDuration());
        res.setTimezone(meeting.getTimezone());
        res.setType(meeting.getType());
        res.setAppointmentId(meeting.getAppointment().getId());

        return res;
    }
}