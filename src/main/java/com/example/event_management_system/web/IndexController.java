package com.example.event_management_system.web;

import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.web.dto.LoginRequest;
import com.example.event_management_system.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView mv = new ModelAndView();
        mv.setViewName("login");
        mv.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            mv.addObject("errorMessage", "Incorrect username or password!");
        }

        return mv;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView mv = new ModelAndView();
        mv.setViewName("home");
        mv.addObject("user", user);

        return mv;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView mv = new ModelAndView();
        mv.setViewName("register");
        mv.addObject("registerRequest", new RegisterRequest());

        return mv;
    }

    @PostMapping("/register")
    public String registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerRequest);

        return "redirect:/login";

    }
}
