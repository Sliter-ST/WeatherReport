package com.example.weatherreport.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    @SerializedName("city")     //Json字段和Java字段建立联系
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
