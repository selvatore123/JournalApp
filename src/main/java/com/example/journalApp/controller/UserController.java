package com.example.journalApp.controller;

import com.example.journalApp.entity.JournalEntry;
import com.example.journalApp.entity.User;
import com.example.journalApp.services.JournalEntryService;
import com.example.journalApp.services.UserService;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JournalEntryService journalEntryService;


    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        log.info("User is Authenticated with username {} for updateUser", name);

        User userInDb = userService.findByUserName(name);
        userInDb.setUserName(user.getUserName());
        userInDb.setPassword(user.getPassword());
        userService.saveNewEntry(userInDb);

        log.info("User is Updated with username {} for updateUser", name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        log.info("User is Authenticated with username {} for deleteUserById", name);
        User UserRecord = userService.findByUserName(name);
        List<JournalEntry> journalEntries = UserRecord.getJournalEntries();
        if(!journalEntries.isEmpty()){
            for(JournalEntry journalEntry : journalEntries){
                ObjectId id = journalEntry.getId();
                journalEntryService.deleteById(id, name);
            }
            log.debug("User Journal Entries Deleted Successfully");
        }
        userService.deleteByUserName(name);
        log.info("User is Deleted Successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}


