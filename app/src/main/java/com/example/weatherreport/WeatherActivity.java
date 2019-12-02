package com.example.weatherreport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.weatherreport.gson.Forecast;
import com.example.weatherreport.gson.Weather;
import com.example.weatherreport.service.AutoUpdateService;
import com.example.weatherreport.util.HttpUtil;
import com.example.weatherreport.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;

    private String mWeatherId;
    private Button navButton;

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;
    private ImageView sun;

    private Button forecast_btn;
    private LinearLayout forecast;
    private Button aqi_btn;
    private LinearLayout aqi;
    private Button suggestion_btn;
    private LinearLayout suggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        //初始化控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);

        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh) ;
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        sun = (ImageView) findViewById(R.id.sun);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            //从intent中取出天气id，调用requestWeather（）从服务器获取数据
            mWeatherId = getIntent().getStringExtra("weather_id");
//            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        navButton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        }));

        forecast = (LinearLayout) findViewById(R.id.forecast);
        forecast_btn = (Button) findViewById(R.id.forecast_btn);
        forecast_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forecast.getVisibility() == View.VISIBLE){
                forecast.setVisibility(View.GONE);
            }else{
                    forecast.setVisibility(View.VISIBLE);
                }
            }
        });

        aqi = (LinearLayout) findViewById(R.id.aqi);
        aqi_btn = (Button) findViewById(R.id.aqi_btn);
        aqi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aqi.getVisibility() == View.VISIBLE){
                    aqi.setVisibility(View.GONE);
                }else{
                    aqi.setVisibility(View.VISIBLE);
                }
            }
        });

        suggestion = (LinearLayout) findViewById(R.id.suggestion);
        suggestion_btn = (Button) findViewById(R.id.suggestion_btn);
        suggestion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (suggestion.getVisibility() == View.VISIBLE){
                    suggestion.setVisibility(View.GONE);
                }else{
                    suggestion.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 根据天气的id请求城市天气信息
     */
    public void requestWeather(final String weatherId){

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId;       //地址
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {     //向地址发出请求，并以JSON格式返回
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);        //将JSON数据转换成weather对象
                runOnUiThread(new Runnable() {                                              //切换为主线程
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){        //通过服务器返回status判断是否返回成功
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();   //将返回数据储存到SharedPreferences
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

//        loadBingPic();
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }



    public void changeImag(String weatherInfo)
    {
        if(weatherInfo.equals("晴"))
        {
            Glide.with(this).load(R.mipmap.qing).into(sun);
            Glide.with(this).load(R.drawable.sun_pic).into(bingPicImg);

        }
        else if(weatherInfo.equals("多云"))
        {

            Glide.with(this).load(R.mipmap.duoyun).into(sun);
            Glide.with(this).load(R.drawable.cloud_pic).into(bingPicImg);
        }
        else if(weatherInfo.equals("阴"))
        {
            Glide.with(this).load(R.mipmap.yin).into(sun);
            Glide.with(this).load(R.drawable.cloud_pic).into(bingPicImg);
        }
        else if(weatherInfo.equals("雷阵雨"))
        {
            Glide.with(this).load(R.mipmap.leizhenyu).into(sun);
            Glide.with(this).load(R.drawable.bong_pic).into(bingPicImg);
        }
        else if(weatherInfo.equals("阵雨"))
        {
            Glide.with(this).load(R.mipmap.zhongyu).into(sun);
            Glide.with(this).load(R.drawable.rain_pic).into(bingPicImg);
        }
        else if(weatherInfo.equals("小雨"))
        {
            Glide.with(this).load(R.mipmap.xiaoyu).into(sun);
            Glide.with(this).load(R.drawable.rain_pic).into(bingPicImg);
        }
        else if(weatherInfo.equals("中雨"))
        {
            Glide.with(this).load(R.mipmap.zhongyu).into(sun);
            Glide.with(this).load(R.drawable.rain_pic).into(bingPicImg);
        }
        else if(weatherInfo.equals("大雨"))
        {
            Glide.with(this).load(R.mipmap.dayu).into(sun);
            Glide.with(this).load(R.drawable.rain_pic).into(bingPicImg);
        }
        else
        {
//            Glide.with(this).load(bingPic).into(bingPicImg);
            Glide.with(this).load("#fff").into(bingPicImg);          //去掉天气符号
        }
    }


    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText("更新时间"+updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        changeImag(weatherInfo);
//        getWeatherInfo(weatherInfo);

        for (Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);      //后台更新
        startService(intent);
    }
}
