package com.fitpulse.controller;

import com.fitpulse.service.UserService;
import com.fitpulse.service.WorkoutBookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
    private final UserService userService; private final WorkoutBookingService bookingService;
    public ProfileController(UserService userService, WorkoutBookingService bookingService) { this.userService = userService; this.bookingService = bookingService; }
    @GetMapping("/profile")
    public String profile(Model model) { model.addAttribute("user", userService.getCurrentUser()); model.addAttribute("bookings", bookingService.getCurrentUserBookings()); return "profile"; }
}
