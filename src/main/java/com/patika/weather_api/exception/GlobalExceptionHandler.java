package com.patika.weather_api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({WeatherApi4xxException.class})
    public ResponseEntity<Object> handleWeatherApi4xxException(WeatherApi4xxException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(exception.getResponseBody());
    }
}
