package com.fitpulse.controller;

import com.fitpulse.service.WorkoutBookingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/bookings")
public class WorkoutBookingController {
    private final WorkoutBookingService bookingService;
    public WorkoutBookingController(WorkoutBookingService bookingService) { this.bookingService = bookingService; }
    @GetMapping public String myBookings(org.springframework.ui.Model model) { model.addAttribute("bookings", bookingService.getCurrentUserBookings()); return "bookings/list"; }
    @PostMapping("/{id}/cancel") public String cancel(@PathVariable UUID id) { bookingService.cancel(id); return "redirect:/bookings"; }
}
