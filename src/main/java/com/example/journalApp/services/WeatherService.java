package com.example.journalApp.services;

import com.example.journalApp.Cache.AppCache;
import com.example.journalApp.api.response.WeatherResponse;
import com.example.journalApp.constants.Placeholders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class WeatherService {

    @Value("${weather.api.key}")
    public String API_KEY;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppCache appCache;

    public WeatherResponse getWeather(String city){
            String finalApi = Objects.requireNonNull(
                    appCache.appCache.get(AppCache.keys.WEATHER_API.toString()),
                    "WEATHER_API template missing from cache"
            ).replace(Placeholders.API_KEY, API_KEY).replace(Placeholders.CITY, city);

            ResponseEntity<WeatherResponse> response = restTemplate.exchange(
                finalApi,
                Objects.requireNonNull(HttpMethod.GET, "HttpMethod.GET"),
                null,
                WeatherResponse.class
        );
        return response.getBody();
    }
}
