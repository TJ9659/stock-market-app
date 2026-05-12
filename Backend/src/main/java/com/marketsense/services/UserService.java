package com.marketsense.services;

import com.marketsense.dto.UpdatePasswordRequestDto;
import com.marketsense.dto.UpdateProfileRequestDto;
import com.marketsense.dto.UserProfileDto;
import com.marketsense.exceptions.InvalidPasswordException;
import com.marketsense.exceptions.ResourceNotFoundException;
import com.marketsense.exceptions.SamePasswordException;
import com.marketsense.model.User;
import com.marketsense.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserProfileDto updateUserDetails(String email, UpdateProfileRequestDto request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());

        User savedUser = userRepository.save(user);
        return new UserProfileDto(savedUser);

    }

    public UserProfileDto updateUserPassword(String email, UpdatePasswordRequestDto request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password does not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException("New password cannot be the same as your current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        return new UserProfileDto(savedUser);
    }


}
