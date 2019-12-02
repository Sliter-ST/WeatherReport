package com.example.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    //总实例类Weather引用各个实体类
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
