package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.Specialization;
import com.healthcare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/speciality")
public class AnonymousController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/all")
    public StandardDTO<?> getAll() {
        List<Specialization> res = doctorService.getAllSpecializations();
        return new StandardDTO<>(HttpStatus.OK.value(), "Speciality added successfully", res, null);
    }

}
