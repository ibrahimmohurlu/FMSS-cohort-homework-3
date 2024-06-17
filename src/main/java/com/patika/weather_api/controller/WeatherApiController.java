package com.patika.weather_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patika.weather_api.dto.CurrentWeatherResponseDto;
import com.patika.weather_api.dto.WeatherForecastResponseDto;
import com.patika.weather_api.exception.WeatherApi4xxException;
import com.patika.weather_api.model.Forecast;
import com.patika.weather_api.model.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class WeatherApiController {

    @Value("${weather_api.key}")
    private String API_KEY;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient customClient = RestClient.builder()
            .baseUrl("https://api.weatherapi.com/v1")
            .defaultStatusHandler(HttpStatusCode::is4xxClientError, ((request, response) -> {
                // if there is error throw custom exception
                // that will be handled by global handler
                // when throwing send the response body as Map
                // which will include error message

                Map jsonMap = objectMapper.readValue(response.getBody(), Map.class);
                throw new WeatherApi4xxException(jsonMap, HttpStatus.valueOf(response.getStatusCode().value()));
            }))
            .build();


    @GetMapping("/{location}/current")
    public CurrentWeatherResponseDto getCurrentForecast(@PathVariable String location) {
        return customClient
                .get()
                .uri("/current.json?key={API_KEY}&q={location}&aqi=no", API_KEY, location)
                .retrieve()
                .body(CurrentWeatherResponseDto.class);

    }

    @GetMapping("/{location}/weekly")
    public ResponseEntity<WeatherForecastResponseDto> getWeeklyForecast(@PathVariable String location) {

        JsonNode jsonResponse = customClient
                .get()
                .uri("/forecast.json?key={API_KEY}&q={location}&aqi=no&days=7", API_KEY, location)
                .retrieve()
                .body(JsonNode.class);
        List<Forecast> forecasts = parseForecastData(jsonResponse);
        Location responseLocation = parseLocationData(jsonResponse);

        WeatherForecastResponseDto responseDto = WeatherForecastResponseDto.builder()
                .forecast(forecasts)
                .location(responseLocation)
                .build();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{location}/monthly")
    public ResponseEntity<WeatherForecastResponseDto> getMonthlyForecast(@PathVariable String location) {
        /**
         * Because API doesn't have monthly forecast
         * we need to fetch first 14 days (max days API can give us)
         * and then fetch the remaining day individually
         * */
        JsonNode jsonResponse = customClient
                .get()
                .uri("/forecast.json?key={API_KEY}&q={location}&aqi=no&days=14", API_KEY, location)
                .retrieve()
                .body(JsonNode.class);
        List<Forecast> forecasts = parseForecastData(jsonResponse);

        LocalDate date = LocalDate.now().plusDays(14);
        for (int i = 14; i < 30; i++) {
            Forecast forecastByDate = getForecastByDate(date, location);
            forecasts.add(forecastByDate);
            date=date.plusDays(1);
        }

        Location responseLocation = parseLocationData(jsonResponse);

        WeatherForecastResponseDto responseDto = WeatherForecastResponseDto.builder()
                .forecast(forecasts)
                .location(responseLocation)
                .build();
        return ResponseEntity.ok(responseDto);
    }

    private List<Forecast> parseForecastData(JsonNode jsonData) {
        List<Forecast> forecast = new ArrayList<>();
        try {
            Iterator<JsonNode> elements = jsonData.get("forecast").get("forecastday").elements();
            while (elements.hasNext()) {
                JsonNode node = elements.next();
                Forecast currentForecast = Forecast.builder()
                        .date(node.get("date").asText())
                        .averageTempInC(node.get("day").get("avgtemp_c").asDouble())
                        .averageTempInF(node.get("day").get("avgtemp_f").asDouble())
                        .build();
                forecast.add(currentForecast);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return forecast;
    }

    private Location parseLocationData(JsonNode jsonData) {
        Location location = new Location();
        try {
            JsonNode locationNode = jsonData.get("location");
            location.setName(locationNode.get("name").asText());
            location.setRegion(locationNode.get("region").asText());
            location.setCountry(locationNode.get("country").asText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    private Forecast getForecastByDate(LocalDate date, String location) {
        JsonNode jsonNode = customClient
                .get()
                .uri("/future.json?key={API_KEY}&q={location}&aqi=no&dt={date}", API_KEY, location, date.toString())
                .retrieve()
                .body(JsonNode.class);
        return parseForecastData(jsonNode).get(0);
    }
}
