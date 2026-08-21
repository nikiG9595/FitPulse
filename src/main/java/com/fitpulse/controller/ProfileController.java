package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.service.UserService;
import com.fitpulse.service.WorkoutBookingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {
    private final UserService userService;
    private final WorkoutBookingService bookingService;

    public ProfileController(UserService userService, WorkoutBookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("bookings", bookingService.getCurrentUserBookings());
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String edit(Model model) {
        model.addAttribute("profileUpdateRequest", userService.getCurrentUserProfileUpdateRequest());
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String update(@Valid @ModelAttribute ProfileUpdateRequest profileUpdateRequest,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) return "profile-edit";

        try {
            userService.updateCurrentUserProfile(profileUpdateRequest);
            return "redirect:/profile?updated";
        } catch (FitPulseException exception) {
            model.addAttribute("globalError", exception.getMessage());
            return "profile-edit";
        }
    }
}
