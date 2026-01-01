package com.eduforge.platform.web.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletResponse resp) {
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage();
        model.addAttribute("errorMessage", msg);

        String lower = msg.toLowerCase();
        if (lower.contains("accès") || lower.contains("refus") || lower.contains("interdit")) {
            resp.setStatus(403);
            return "error/403";
        }
        if (lower.contains("introuvable") || lower.contains("inexistant") || lower.contains("not found")) {
            resp.setStatus(404);
            return "error/404";
        }
        resp.setStatus(400);
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model, HttpServletResponse resp) {
        resp.setStatus(500);
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage();
        model.addAttribute("errorMessage", msg);
        return "error/500";
    }
}
