package com.patika.weather_api.dto;

import com.patika.weather_api.model.Current;
import com.patika.weather_api.model.Location;
import lombok.Data;

@Data
public class CurrentWeatherResponseDto {
    private Location location;
    private Current current;
}
