package com.example.event_management_system.web;

import com.example.event_management_system.exception.NotificationServiceFeignCallException;
import com.example.event_management_system.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameAlreadyExist(HttpServletRequest request, RedirectAttributes redirectAttributes, UsernameAlreadyExistsException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(com.example.event_management_system.exception.NotificationServiceFeignCallException.class)
    public String handleNotificationFeignCallException(RedirectAttributes redirectAttributes, NotificationServiceFeignCallException exception) {
        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("clearHistoryErrorMessage", message);
        return "redirect:/notifications";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions(Exception exception) {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}