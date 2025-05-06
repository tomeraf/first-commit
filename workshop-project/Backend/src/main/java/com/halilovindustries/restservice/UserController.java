package com.halilovindustries.restservice;

import org.springframework.web.bind.annotation.RestController;

import Domain.Response;
import Service.UserService;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/user")
public class UserController {
    protected UserService userService;


    public UserController(UserService userService) {
        this.userService  = userService;
    }
    
    @GetMapping("/enter")
    public Response<String> enterToSystem() {
        return userService.enterToSystem();
    }
    @PostMapping("/register")
    public Response<Void> registerUser(
            @RequestHeader("X-Session-Token") String token,
            @RequestBody RegisterRequest req) {
        return userService.registerUser(
                token,
                req.getUsername(),
                req.getPassword(),
                req.getDateOfBirth()
        );
    }

    @PostMapping("/login")
    public Response<String> loginUser(
            @RequestHeader("X-Session-Token") String token,
            @RequestBody LoginRequest req) {
        return userService.loginUser(
                token,
                req.getUsername(),
                req.getPassword()
        );
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private LocalDate dateOfBirth;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    }

    public static class LoginRequest {
        private String username;
        private String password;
    
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
