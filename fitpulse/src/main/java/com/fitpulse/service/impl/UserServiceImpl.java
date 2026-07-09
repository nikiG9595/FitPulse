package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository; this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) throw new FitPulseException("Passwords do not match");
        if (userRepository.existsByUsername(request.getUsername())) throw new FitPulseException("Username already exists");
        if (userRepository.existsByEmail(request.getEmail())) throw new FitPulseException("Email already exists");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.MEMBER);
        userRepository.save(user);
    }
    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new FitPulseException("Current user not found"));
    }
    @Override
    public List<User> getAllMembers() { return userRepository.findAll(); }
}
