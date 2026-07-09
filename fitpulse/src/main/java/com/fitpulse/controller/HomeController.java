package com.fitpulse.controller;

import com.fitpulse.service.GymClassService;
import com.fitpulse.service.MembershipService;
import com.fitpulse.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final GymClassService gymClassService;
    private final MembershipService membershipService;
    private final UserService userService;

    public HomeController(GymClassService gymClassService, MembershipService membershipService, UserService userService) {
        this.gymClassService = gymClassService;
        this.membershipService = membershipService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("classes", gymClassService.getAll());
        model.addAttribute("memberships", membershipService.getAll());

        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("classes", gymClassService.getAll());
        model.addAttribute("user", userService.getCurrentUser());

        return "dashboard";
    }
}