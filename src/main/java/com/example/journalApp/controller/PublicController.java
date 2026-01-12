package com.example.journalApp.controller;

import com.example.journalApp.entity.User;
import com.example.journalApp.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/health-check")
    public String healthCheck(){
        log.info("Health Check Started");
        return "Ok";
    }

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            userService.saveNewEntry(user);
            log.info("User created Successfully: {}", user.getUserName());
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
            log.error("User creation failed for user: {}, {}", user.getUserName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }
}
