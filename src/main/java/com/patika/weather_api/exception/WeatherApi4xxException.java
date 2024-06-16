package com.patika.weather_api.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Map;

@Getter
@Setter
@Builder
public class WeatherApi4xxException extends RuntimeException {
    private Map responseBody;
    private HttpStatus status;

    public WeatherApi4xxException(Map responseBody, HttpStatus status) {
        this.responseBody = responseBody;
        this.status = status;
    }
}
