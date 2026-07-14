package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.GymClassRequest;
import com.fitpulse.model.enums.ClassIntensity;
import com.fitpulse.service.GymClassService;
import com.fitpulse.service.MembershipService;
import com.fitpulse.service.WorkoutBookingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/classes")
public class GymClassController {
    private final GymClassService gymClassService;
    private final MembershipService membershipService;
    private final WorkoutBookingService bookingService;

    public GymClassController(GymClassService gymClassService, MembershipService membershipService, WorkoutBookingService bookingService) {
        this.gymClassService = gymClassService;
        this.membershipService = membershipService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String all(Model model) {
        model.addAttribute("classes", gymClassService.getAll());
        model.addAttribute("bookingService", bookingService);
        return "classes/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        model.addAttribute("gymClass", gymClassService.getById(id));
        model.addAttribute("bookingsCount", bookingService.countBookingsForClass(id));
        return "classes/details";
    }

    @GetMapping("/create")
    public String create(Model model) {
        prepareForm(model, new GymClassRequest());
        return "classes/form";
    }

    @PostMapping("/create")
    public String doCreate(@Valid @ModelAttribute GymClassRequest gymClassRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, gymClassRequest);
            return "classes/form";
        }
        gymClassService.create(gymClassRequest);
        return "redirect:/classes";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        prepareForm(model, gymClassService.mapToRequest(id));
        model.addAttribute("classId", id);
        return "classes/form";
    }

    @PostMapping("/{id}/edit")
    public String doEdit(@PathVariable UUID id, @Valid @ModelAttribute GymClassRequest gymClassRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, gymClassRequest);
            model.addAttribute("classId", id);
            return "classes/form";
        }
        gymClassService.update(id, gymClassRequest);
        return "redirect:/classes/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        gymClassService.delete(id);
        return "redirect:/classes";
    }

    @PostMapping("/{id}/book")
    public String book(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.book(id);
            redirectAttributes.addFlashAttribute("success", "Class booked successfully.");
        } catch (FitPulseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/classes/" + id;
    }

    private void prepareForm(Model model, GymClassRequest request) {
        model.addAttribute("gymClassRequest", request);
        model.addAttribute("memberships", membershipService.getAll());
        model.addAttribute("intensities", ClassIntensity.values());
    }
}
