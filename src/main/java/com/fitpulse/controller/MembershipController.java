package com.fitpulse.controller;

import com.fitpulse.model.dto.MembershipRequest;
import com.fitpulse.model.enums.MembershipType;
import com.fitpulse.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/memberships")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public String all(Model model) {
        model.addAttribute("memberships", membershipService.getAll());
        return "memberships/list";
    }

    @PostMapping("/{id}/choose")
    public String choose(@PathVariable UUID id) {
        membershipService.chooseMembership(id);
        return "redirect:/profile";
    }

    @GetMapping("/create")
    public String create(Model model) {
        prepare(model, new MembershipRequest());
        return "memberships/form";
    }

    @PostMapping("/create")
    public String doCreate(@Valid @ModelAttribute MembershipRequest membershipRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            prepare(model, membershipRequest);
            return "memberships/form";
        }
        membershipService.create(membershipRequest);
        return "redirect:/memberships";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        prepare(model, membershipService.mapToRequest(id));
        model.addAttribute("membershipId", id);
        return "memberships/form";
    }

    @PostMapping("/{id}/edit")
    public String doEdit(@PathVariable UUID id, @Valid @ModelAttribute MembershipRequest membershipRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            prepare(model, membershipRequest);
            model.addAttribute("membershipId", id);
            return "memberships/form";
        }
        membershipService.update(id, membershipRequest);
        return "redirect:/memberships";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        membershipService.delete(id);
        return "redirect:/memberships";
    }

    private void prepare(Model model, MembershipRequest request) {
        model.addAttribute("membershipRequest", request);
        model.addAttribute("types", MembershipType.values());
    }
}
