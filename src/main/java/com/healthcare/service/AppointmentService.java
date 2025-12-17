package com.healthcare.service;

import com.healthcare.dto.AppointmentResponse;
import com.healthcare.entity.Appointment;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.DoctorSlot;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AppointmentRepo;
import com.healthcare.repository.DoctorSlotRepository;
import com.healthcare.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private DoctorSlotRepository slotRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private EmailService emailService;

    public AppointmentResponse bookSlot(Long slotId, String token) throws UserException {
        DoctorSlot slot = slotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found with id: " + slotId));
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        if (!slot.isAvailable()) {
            throw new RuntimeException("Slot already booked");
        } else if (!sessionUser.getActive() || !sessionUser.getRole().equalsIgnoreCase("USER")) {
            throw new RuntimeException("You are not allowed to book Appointment");
        }

        slot.setAvailable(false);
        slotRepo.save(slot);

        Appointment appointment = new Appointment();
        appointment.setUser(sessionUser);
        appointment.setSlot(slot);
        appointment.setStatus("BOOKED");

        appointmentRepo.save(appointment);

        AppointmentResponse res = new AppointmentResponse();
        res.setAppointmentId(appointment.getId());
        res.setAppointmentStatus(appointment.getStatus());
        res.setUser(sessionUser);
        res.setSlotId(slot.getId());
        res.setSlotType(slot.getSlotType());
        res.setEndTime(slot.getEndTime());
        res.setStartTime(slot.getStartTime());
        res.setSpecialization(slot.getDoctor().getSpecialization());
        res.setDoctorId(slot.getDoctor().getId());
        res.setDoctorName(slot.getDoctor().getUser().getFirstName() + " " + slot.getDoctor().getUser().getLastName());

        return res;
    }

    public List<Map<String, Object>> getAppointmentsForDoctor(Long doctorId, String token) throws UserException {
        Doctor doc = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with provided id"));
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        if (!sessionUser.getRole().equalsIgnoreCase("DOCTOR") || !doc.getUser().getEmail().equals(sessionUser.getEmail())) {
            throw new UsernameNotFoundException("You are not the owner of this list of appointments");
        }

        List<Appointment> appointments = appointmentRepo.findBySlotDoctorId(doctorId);

        List<Map<String, Object>> res = new ArrayList<>();

        for (Appointment appointment : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("slotId", appointment.getSlot().getId());
            map.put("appointmentId", appointment.getId());
            map.put("patientName", appointment.getUser().getFirstName() + " " + appointment.getUser().getLastName());
            map.put("patientEmail", appointment.getUser().getEmail());
            map.put("patientMobile", appointment.getUser().getMobile());
//            map.put("doctorId", appointment.getSlot().getDoctor().getId());
//            map.put("doctorEmail", appointment.getSlot().getDoctor().getUser().getEmail());
//            map.put("doctorMobile", appointment.getSlot().getDoctor().getUser().getMobile());
            map.put("slotType", appointment.getSlot().getSlotType());
            map.put("startTime", appointment.getSlot().getStartTime());
            map.put("endTime", appointment.getSlot().getEndTime());
            map.put("status", appointment.getStatus());

            res.add(map);
        }

        return res;
    }

    public List<Appointment> getBookedAppointmentsOfUser(String token) throws UserException {
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        return appointmentRepo.findByUserId(sessionUser.getId());
    }

    public void sendEmailWithRoomId(Long appointmentId, String token) throws UserException {
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (sessionUser.getRole().equals("USER") || sessionUser.getRole().equals("DOCTOR")) {
            LocalDateTime dateTime = LocalDateTime.parse(appointment.getSlot().getStartTime().toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = dateTime.format(formatter);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            String formattedTime = dateTime.format(timeFormatter);

            String roomString = appointment.getUser().getEmail() + "_"
                    + appointment.getSlot().getDoctor().getUser().getEmail() + "_"
                    + appointment.getId();
            String roomId = UUID.nameUUIDFromBytes(roomString.getBytes(StandardCharsets.UTF_8)).toString();

            String mailContent = "Appointment Id: " + appointment.getId() + "\n"
                    + "Patient Email: " + appointment.getUser().getEmail() + "\n"
                    + "Doctor Email: " + appointment.getSlot().getDoctor().getUser().getEmail() + "\n"
                    + "Date: " + formattedDate + "\n"
                    + "Time Slot: " + formattedTime + "\n"
                    + "Room Id: " + roomId;

            if (!appointment.isCallEmailSent()) {
                emailService.sendSimpleMessage(appointment.getUser().getEmail(), "Room Id for video call", mailContent);
                emailService.sendSimpleMessage(appointment.getSlot().getDoctor().getUser().getEmail(), "Room Id for video call", mailContent);
                appointment.setCallEmailSent(true);
                appointmentRepo.save(appointment);
            }
        } else {
            throw new IllegalArgumentException("You are not allowed to start this call");
        }
    }

    public Appointment findAppointmentBySlotId(Long slotId, String token) throws UserException {
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        if (sessionUser.getEmail().equals(jwtUtil.extractEmail(token))) {
            return appointmentRepo.findBySlotId(slotId)
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with slot Id: " + slotId));
        } else {
            throw new IllegalArgumentException("Unauthorized entity");
        }
    }

    public List<Appointment> getAllAppointments(String actualToken) throws NoSuchFieldException {
        List<Appointment> appointments = appointmentRepo.findAll();
         if(appointments.isEmpty()) {
             throw new NoSuchFieldException("No appointment found");
         }
         return appointments;
    }
}