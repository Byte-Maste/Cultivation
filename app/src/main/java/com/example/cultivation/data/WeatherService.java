package com.example.cultivation.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units);

    @GET("data/2.5/forecast")
    Call<ForecastResponse> getForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units);
}
