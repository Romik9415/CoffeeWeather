package com.example.romankhrupa.coffeeweather;

import android.util.Log;

import java.text.DateFormatSymbols;

/**
 * Created by romankhrupa on 10/3/17.
 */

public class WeekDays {
            DateFormatSymbols dfs = new DateFormatSymbols();

        public String getWeekday(int days){
            String[] weekdays = dfs.getWeekdays();
            Log.i("day",weekdays[days]);
            return weekdays[days];
        }
}
