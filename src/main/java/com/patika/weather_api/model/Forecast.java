package com.patika.weather_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Forecast {
    private String date;
    @JsonProperty("average_temp_c")
    private Double averageTempInC;
    @JsonProperty("average_temp_f")
    private Double averageTempInF;
}
