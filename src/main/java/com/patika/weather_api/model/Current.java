package com.patika.weather_api.model;

import lombok.Data;

@Data
public class Current {
    private String last_updated;
    private Double temp_c;
    private Double temp_f;
}
