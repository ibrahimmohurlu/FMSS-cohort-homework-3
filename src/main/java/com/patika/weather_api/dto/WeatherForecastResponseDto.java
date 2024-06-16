package com.patika.weather_api.dto;

import com.patika.weather_api.model.Forecast;
import com.patika.weather_api.model.Location;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class WeatherForecastResponseDto {
    private Location location;
    private List<Forecast> forecast;
}
