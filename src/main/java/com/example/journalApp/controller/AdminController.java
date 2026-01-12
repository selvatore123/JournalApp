package com.example.journalApp.controller;


import com.example.journalApp.entity.User;
import com.example.journalApp.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(){
        log.debug("Get request received - GetAllUsers");
        List<User> all = userService.getAll();
        if(all != null && !all.isEmpty()){
            log.info("Getting all users from database");
            return new ResponseEntity<>(all, HttpStatus.OK);
        }else{
            log.info("No user found from database");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create-admin-user")
    public void createUser(@RequestBody User user){
        log.debug("Post request received - CreateUser");
        userService.saveAdmin(user);
    }
}
