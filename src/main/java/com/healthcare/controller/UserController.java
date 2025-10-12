package com.healthcare.controller;

import com.healthcare.dto.AppointmentResponse;
import com.healthcare.dto.BookedAppointment;
import com.healthcare.dto.StandardDTO;
import com.healthcare.dto.UpdateSlotResponse;
import com.healthcare.entity.Appointment;
import com.healthcare.entity.Doctor;
import com.healthcare.exception.UserException;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, User!";
    }

    @GetMapping("/doctor-by-speciality")
    public StandardDTO<List<Map<String, Object>>> getDoctorBySpeciality(@RequestParam("speciality") String speciality) {
        List<Doctor> doctors = doctorService.getDoctorBySpeciality(speciality);

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Doctor doc : doctors) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("specialization", doc.getSpecialization());
            map.put("verificationStatus", doc.getVerificationStatus());
            map.put("doctor", doc.getUser());
            responseList.add(map);
        }

        return new StandardDTO<>(HttpStatus.OK.value(), "Doctors fetched successfully by speciality", responseList, null);
    }

    @PutMapping("/book-appointment")
    public ResponseEntity<StandardDTO<AppointmentResponse>> bookAppointment(
            @RequestParam("slotId") Long slotId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            String actualToken = token.replace("Bearer ", "");
            AppointmentResponse response = appointmentService.bookSlot(slotId, actualToken);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Appointment booked successfully", response, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @GetMapping("/booked/appointments")
    public StandardDTO<List<BookedAppointment>> getMyBookedAppointments(@RequestHeader("Authorization") String token) throws UserException {
        try {
            String actualToken = token.replace("Bearer ", "");
            List<Appointment> appList = appointmentService.getBookedAppointmentsOfUser(actualToken);

            List<BookedAppointment> res = new ArrayList<>();

            for (Appointment a : appList) {
                BookedAppointment ba = new BookedAppointment();
                ba.setAppointmentId(a.getId());
                ba.setUser(a.getUser());
                ba.setAppointmentStatus(a.getStatus());
                ba.setDoctorFirstName(a.getSlot().getDoctor().getUser().getFirstName());
                ba.setDoctorLastName(a.getSlot().getDoctor().getUser().getLastName());
                ba.setDoctorMobile(a.getSlot().getDoctor().getUser().getMobile());
                ba.setDoctorId(a.getSlot().getDoctor().getUser().getId());
                ba.setEndTime(a.getSlot().getEndTime());
                ba.setSpecialization(a.getSlot().getDoctor().getSpecialization());
                ba.setSlotId(a.getSlot().getId());
                ba.setSlotType(a.getSlot().getSlotType());
                ba.setStartTime(a.getSlot().getStartTime());
                ba.setDoctorEmail(a.getSlot().getDoctor().getUser().getEmail());

                res.add(ba);
            }

            Map<String, Object> metaData = new HashMap<>();
            metaData.put("count", res.size());

            return new StandardDTO<>(HttpStatus.OK.value(), "Appointments fetched successfully", res, metaData);
        } catch (Exception e) {
            return new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null);
        }
    }
}
