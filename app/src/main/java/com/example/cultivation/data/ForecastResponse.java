package com.example.cultivation.data;

import java.util.List;

public class ForecastResponse {
    public List<ForecastItem> list;

    public static class ForecastItem {
        public long dt; // Unix timestamp
        public Main main;
        public List<Weather> weather;
        public String dt_txt; // "2022-08-30 15:00:00"
    }

    public static class Main {
        public float temp;
        public float temp_min;
        public float temp_max;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
    }
}
