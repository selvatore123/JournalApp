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


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/journal")
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllJournalEntriesOfUser(){
        log.debug("Get request received - GetAllJournalEntriesOfUser");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        log.info("User is Authenticated : {} for getAllJournalEntriesOfUser", userName);
        User user = userService.findByUserName(userName);
        List<JournalEntry> all = user.getJournalEntries();

        if(all != null && !all.isEmpty()){
            log.info("Journal Entries Found : for getAllJournalEntriesOfUser {}", all);
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        log.info("Journal Entries Not Found for getAllJournalEntriesOfUser {}", all);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myEntry){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            log.debug("User is Authenticated : {} for CreateEntry", userName);
            journalEntryService.saveEntry(myEntry,userName);
            log.info("Journal Entry Created for : {}", myEntry.getName());
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        }catch(Exception e){
            log.error("Error while saving entry : {}", myEntry, e);
            return new ResponseEntity<>(myEntry, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/id/{my_id}") //Ram can only be see his journal Entry by id. Kisi or ki journal Entry koi or user na dekh ske.
    public ResponseEntity<JournalEntry> getEntryById(@PathVariable ObjectId my_id){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String userName = authentication.getName();
       log.debug("User is Authenticated : {} for GetEntryById", userName);
       User user = userService.findByUserName(userName);
        List<JournalEntry> collect = user.getJournalEntries().stream().filter(x -> x.getId().equals(my_id)).collect(Collectors.toList());
        if(!collect.isEmpty()){
            log.info("Journal Entries Found for Id : {}", my_id);
            Optional<JournalEntry> journalEntry = journalEntryService.findById(my_id);
            if(journalEntry.isPresent()){
                log.info("Journal Entry Found : {}, for getEntryById", journalEntry);
                return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
            }
        }
        log.info("Journal Entry Not Found for getEntryById, Id : {}", my_id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/id/{my_id}")
    public ResponseEntity<JournalEntry> deleteEntryById(@PathVariable ObjectId my_id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        log.debug("User is Authenticated : {} for DeleteEntryById", userName);
        boolean removed = journalEntryService.deleteById(my_id, userName);
        if(removed){
            log.info("Journal Entry Deleted for ID : {} in deleteEntryByID", my_id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            log.info("Journal Entry Not Found for DeleteEntryById, Id : {}", my_id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<JournalEntry> updateJournalEntryById(@PathVariable ObjectId id,
                                                               @RequestBody JournalEntry myEntry){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        log.info("User is Authenticated : {} for UpdateJournalEntryById", userName);
        User user = userService.findByUserName(userName);
        List<JournalEntry> collect = user.getJournalEntries().stream().filter(x -> x.getId().equals(id)).collect(Collectors.toList());

        if(!collect.isEmpty()){
            log.info("Journal Entries Found for Id : {} for updateJournalEntryById", id);
            Optional<JournalEntry> journalEntry = journalEntryService.findById(id);
            if(journalEntry.isPresent()){
                JournalEntry oldEntry = journalEntry.get();
                oldEntry.setName(!myEntry.getName().isEmpty() ? myEntry.getName() : oldEntry.getName());
                oldEntry.setContent(myEntry.getContent() != null && !myEntry.getContent().isEmpty() ? myEntry.getContent() : oldEntry.getContent());

                journalEntryService.saveEntry(oldEntry);
                log.info("Journal Entry Updated for ID : {} for updateJournalEntryById", id);
                return new ResponseEntity<>(oldEntry, HttpStatus.OK);
            }
        }
        log.info("You are Authenticated : {} and giving id of another user, Your Id : {}", userName, id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
