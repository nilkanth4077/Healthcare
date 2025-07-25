package com.e_commerce.service;

import com.e_commerce.repository.UserRepo;
import com.e_commerce.util.JwtUtil;
import org.springframework.stereotype.Service;

import com.e_commerce.entity.User;
import com.e_commerce.exception.UserException;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;

    public UserService(UserRepo userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    public User findCustomUserById(Long userId) throws UserException {
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UserException("User not found with id: " + userId);
    }

    public Optional<User> getProfileByToken(String token) throws UserException {
        String email = jwtUtil.extractEmail(token);

        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserException("User not found with email: " + email);
        }
        return user;
    }

}
