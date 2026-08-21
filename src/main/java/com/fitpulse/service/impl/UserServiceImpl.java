package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new FitPulseException("Passwords do not match");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new FitPulseException("Username already exists");
        if (userRepository.existsByEmail(request.getEmail())) throw new FitPulseException("Email already exists");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.MEMBER);
        userRepository.save(user);
        LOGGER.info("User registered: userId={}", user.getId());
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new FitPulseException("Current user not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void changeRole(UUID userId, UserRole newRole) {
        if (userId == null) throw new FitPulseException("A valid user ID is required");
        if (newRole == null) throw new FitPulseException("Role must be either MEMBER or ADMIN");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FitPulseException("User not found"));
        User currentUser = getCurrentUser();

        if (user.getId().equals(currentUser.getId()) && user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN)
            throw new FitPulseException("You cannot remove your own ADMIN role");

        UserRole previousRole = user.getRole();
        if (previousRole == newRole) throw new FitPulseException("User already has the selected role");

        user.setRole(newRole);
        userRepository.save(user);
        LOGGER.info("User role changed: userId={}, previousRole={}, newRole={}, changedByUserId={}",
                user.getId(), previousRole, newRole, currentUser.getId());
    }

    @Override
    public ProfileUpdateRequest getCurrentUserProfileUpdateRequest() {
        User currentUser = getCurrentUser();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername(currentUser.getUsername());
        request.setEmail(currentUser.getEmail());
        return request;
    }

    @Override
    public void updateCurrentUserProfile(ProfileUpdateRequest request) {
        if (request == null) throw new FitPulseException("Profile details are required");

        User currentUser = getCurrentUser();
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), currentUser.getId()))
            throw new FitPulseException("Username already exists");
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), currentUser.getId()))
            throw new FitPulseException("Email already exists");

        currentUser.setUsername(request.getUsername());
        currentUser.setEmail(request.getEmail());
        userRepository.save(currentUser);
        refreshAuthenticationUsername(request.getUsername());
        LOGGER.info("User profile updated: userId={}", currentUser.getId());
    }

    private void refreshAuthenticationUsername(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken updatedAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                username, authentication.getCredentials(), authentication.getAuthorities());
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }
}
