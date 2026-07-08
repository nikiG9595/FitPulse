package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FitPulseException.class)
    public String handleFitPulse(FitPulseException exception, Model model) { model.addAttribute("message", exception.getMessage()); return "error/custom-error"; }
}
