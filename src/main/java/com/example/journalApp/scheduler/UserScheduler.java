package com.example.journalApp.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.journalApp.repository.UserRepositoryImpl;
import com.example.journalApp.services.EmailService;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import com.example.journalApp.entity.User;
import com.example.journalApp.enums.Sentiments;
import com.example.journalApp.Cache.AppCache;
import com.example.journalApp.entity.JournalEntry;
import java.util.HashMap;
import java.util.Map;

public class UserScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;


    @Autowired
    private AppCache appCache;

    @Scheduled(cron = "0 0 0 9 * * SUN")  //run at 9:00 AM on Every Sunday
    public void fetchUsersForSentimentAnalysis(){
        List<User> users = userRepositoryImpl.getUserForSentimentAnalysis(); //get all users that have sentiment analysis enabled
        for(User user : users){
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<Sentiments> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7,ChronoUnit.DAYS))).map(x -> x.getSentiment()).collect(Collectors.toList());   //get all entries from the last 7 days
            Map<Sentiments, Integer> sentimentCount = new HashMap<>(); //count the frequency of each sentiment

            for(Sentiments sentiment : sentiments){
                if(sentiment != null){
                    sentimentCount.put(sentiment, sentimentCount.getOrDefault(sentiment, 0) + 1); //increment the count for the sentiment
                }
            }
            Sentiments mostFrequentSentiment = null; //find the most frequent sentiment
            int maxCount = 0;
            for(Map.Entry<Sentiments, Integer> entry : sentimentCount.entrySet()){
                if(entry.getValue() > maxCount){ //find the most frequent sentiment by comparing the counts
                    maxCount = entry.getValue();
                    mostFrequentSentiment = entry.getKey(); //set the most frequent sentiment
                }
            }
            if(mostFrequentSentiment != null){ //send an email to the user with the most frequent sentiment
                emailService.sendEmail(user.getEmail(), "Sentiment Analysis", "Your most frequent sentiment is: " + mostFrequentSentiment.toString());
            }
        }
    }
    
    @Scheduled(cron = "0 0/10 * ? * *")  //run every 10 minutes
    public void clearAppCache(){
        appCache.init();
    }
}
