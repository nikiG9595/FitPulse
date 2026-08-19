package com.fitpulse.controller;

import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/progress")
public class ProgressController {
    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public String history(Model model) {
        model.addAttribute("progressRecords", progressService.getCurrentUserProgress());
        return "progress/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        ProgressFormRequest request = new ProgressFormRequest();
        request.setRecordedAt(LocalDate.now());
        model.addAttribute("progressFormRequest", request);
        return "progress/form";
    }

    @PostMapping("/create")
    public String doCreate(@Valid @ModelAttribute ProgressFormRequest progressFormRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "progress/form";
        }
        try {
            progressService.create(progressFormRequest);
            redirectAttributes.addFlashAttribute("success", "Progress record added successfully.");
            return "redirect:/progress";
        } catch (ProgressServiceException exception) {
            model.addAttribute("globalError", exception.getMessage());
            return "progress/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        ProgressResponse progress = progressService.getCurrentUserProgress(id);
        ProgressFormRequest request = new ProgressFormRequest();
        request.setWeight(progress.weight());
        request.setBodyFatPercentage(progress.bodyFatPercentage());
        request.setRecordedAt(progress.recordedAt());
        request.setNote(progress.note());
        model.addAttribute("progressFormRequest", request);
        model.addAttribute("progressId", id);
        return "progress/form";
    }

    @PostMapping("/{id}/edit")
    public String doEdit(@PathVariable UUID id,
                         @Valid @ModelAttribute ProgressFormRequest progressFormRequest,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("progressId", id);
            return "progress/form";
        }
        try {
            progressService.update(id, progressFormRequest);
            redirectAttributes.addFlashAttribute("success", "Progress record updated successfully.");
            return "redirect:/progress";
        } catch (ProgressServiceException exception) {
            model.addAttribute("progressId", id);
            model.addAttribute("globalError", exception.getMessage());
            return "progress/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            progressService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Progress record deleted successfully.");
        } catch (ProgressServiceException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/progress";
    }
}
