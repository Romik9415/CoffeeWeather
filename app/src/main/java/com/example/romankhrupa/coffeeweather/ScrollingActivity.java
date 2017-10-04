package com.example.romankhrupa.coffeeweather;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kwabenaberko.openweathermaplib.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;
import com.kwabenaberko.openweathermaplib.models.threehourforecast.ThreeHourForecast;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class ScrollingActivity extends AppCompatActivity {

    public final static String TAG = "ScrollingActivity";
    OpenWeatherMapHelper helper;
    CollapsingToolbarLayout  mCollapsingToolbarLayout;
    TextView weather_description;
    ImageView weather_image_status;
    //Sorry for hardcoding
    TextView weekday2;
    TextView weekday3;
    TextView weekday4;
    TextView weekday5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        weather_description= (TextView) findViewById(R.id.weather_description);
        mCollapsingToolbarLayout.setTitle("City"+" 0°C");
        weather_image_status = (ImageView)findViewById(R.id.weather_image_status);
        weekday2 =  (TextView)findViewById(R.id.day2);
        weekday3 =(TextView)findViewById(R.id.day3);
        weekday4 = (TextView)findViewById(R.id.day4);
        weekday5 = (TextView)findViewById(R.id.day5);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Updated ^-^", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateInfo();
            }
        });

        helper = new OpenWeatherMapHelper();
        helper.setApiKey(getString(R.string.OPEN_WEATHER_MAP_API_KEY));
        helper.setUnits(Units.METRIC);

/*
* https://android-arsenal.com/details/1/6020
* */
        new RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean ok) throws Exception {
                if(ok){
                    updateInfo();
                }
            }
        });

        SmartLocation.with(this)
                .location( new LocationGooglePlayServicesWithFallbackProvider(getApplicationContext()))
                .config(LocationParams.BEST_EFFORT)
                .start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                Toast.makeText(ScrollingActivity.this,
                        ""+location.getLatitude(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateInfo() {
        Log.e(TAG, "updateInfo");
        Location location = SmartLocation.with(this).location().getLastLocation();
        if(location != null) {
            helper.getCurrentWeatherByGeoCoordinates(location.getLatitude(), location.getLongitude(), new OpenWeatherMapHelper.CurrentWeatherCallback() {
                @Override
                public void onSuccess(CurrentWeather currentWeather) {
                    Log.e(TAG,
                            "Coordinates: " + currentWeather.getCoord().getLat() + ", " + currentWeather.getCoord().getLat() + "\n"
                                    + "Weather Description: " + currentWeather.getWeatherArray().get(0).getDescription() + "\n"
                                    + "Max Temperature: " + currentWeather.getMain().getTempMax() + "\n"
                                    + "Wind Speed: " + currentWeather.getWind().getSpeed() + "\n"
                                    + "City, Country: " + currentWeather.getName() + ", " + currentWeather.getSys().getCountry()
                    );
                    setCurrentWeatherInfo(currentWeather.getName(),
                            currentWeather.getMain().getTemp(),
                            currentWeather.getWeatherArray().get(0).getDescription(),
                            currentWeather.getWeatherArray().get(0).getIcon());
                    sendNotification();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.v(TAG, throwable.getMessage());
                }
            });
        }
        updateInfoFor5days(location);
    }
    private void updateInfoFor5days(Location location) {
        helper.getThreeHourForecastByGeoCoordinates(location.getLatitude(), location.getLongitude(), new OpenWeatherMapHelper.ThreeHourForecastCallback() {
            @Override
            public void onSuccess(ThreeHourForecast threeHourForecast) {
                Log.v(TAG,
                        "City/Country: " + threeHourForecast.getCity().getName() + "/" + threeHourForecast.getCity().getCountry() + "\n"
                                + "Forecast Array Count: " + threeHourForecast.getCnt() + "\n"
                                //For this example, we are logging details of only the first forecast object in the forecasts array
                                + "fff"+threeHourForecast.getThreeHourWeatherArray().get(1).getWeatherArray().get(0).getDescription()
                                + "First Forecast Date Timestamp: " + threeHourForecast.getThreeHourWeatherArray().get(0).getDt() + "\n"
                                + "First Forecast Weather Description: " + threeHourForecast.getThreeHourWeatherArray().get(0).getWeatherArray().get(0).getDescription() + "\n"
                                + "First Forecast Max Temperature: " + threeHourForecast.getThreeHourWeatherArray().get(0).getMain().getTempMax() + "\n"
                                + "First Forecast Wind Speed: " + threeHourForecast.getThreeHourWeatherArray().get(0).getWind().getSpeed() + "\n"
                );
                TextView wethearDiscription1 = findViewById(R.id.weather_description1);
                TextView wethearDiscription2 = findViewById(R.id.weather_description2);
                TextView wethearDiscription3 = findViewById(R.id.weather_description3);
                TextView wethearDiscription4 = findViewById(R.id.weather_description4);
                TextView wethearDiscription5 = findViewById(R.id.weather_description5);

                TextView temp1 = findViewById(R.id.temp1);
                TextView temp2 = findViewById(R.id.temp2);
                TextView temp3 = findViewById(R.id.temp3);
                TextView temp4 = findViewById(R.id.temp4);
                TextView temp5 = findViewById(R.id.temp5);

                ImageView imageView1 = findViewById(R.id.weather_image_status1);
                ImageView imageView2 = findViewById(R.id.weather_image_status2);
                ImageView imageView3 = findViewById(R.id.weather_image_status3);
                ImageView imageView4 = findViewById(R.id.weather_image_status4);
                ImageView imageView5 = findViewById(R.id.weather_image_status5);


                wethearDiscription1.setText(threeHourForecast.getThreeHourWeatherArray().get(7).getWeatherArray().get(0).getDescription());
                wethearDiscription2.setText(threeHourForecast.getThreeHourWeatherArray().get(15).getWeatherArray().get(0).getDescription());
                wethearDiscription3.setText(threeHourForecast.getThreeHourWeatherArray().get(23).getWeatherArray().get(0).getDescription());
                wethearDiscription4.setText(threeHourForecast.getThreeHourWeatherArray().get(31).getWeatherArray().get(0).getDescription());
                wethearDiscription5.setText(threeHourForecast.getThreeHourWeatherArray().get(38).getWeatherArray().get(0).getDescription());

                temp1.setText(String.valueOf(threeHourForecast.getThreeHourWeatherArray().get(7).getMain().getTemp()+"°C"));
                temp2.setText(String.valueOf(threeHourForecast.getThreeHourWeatherArray().get(15).getMain().getTemp()+"°C"));
                temp3.setText(String.valueOf(threeHourForecast.getThreeHourWeatherArray().get(23).getMain().getTemp()+"°C"));
                temp4.setText(String.valueOf(threeHourForecast.getThreeHourWeatherArray().get(31).getMain().getTemp()+"°C"));
                temp5.setText(String.valueOf(threeHourForecast.getThreeHourWeatherArray().get(38).getMain().getTemp()+"°C"));

                int currentTime = Calendar.getInstance().getTime().getHours();
                String timeStatus;
                if (currentTime>20||currentTime<6){
                    timeStatus = "n";
                }
                else{
                    timeStatus = "d";
                }
                Resources resources = getApplicationContext().getResources();

                final int resourceId1 = resources.getIdentifier(
                                timeStatus+
                                threeHourForecast.getThreeHourWeatherArray().get(7).getWeatherArray().get(0).getIcon(),
                        "drawable", getApplicationContext().getPackageName());
                imageView1.setImageResource(resourceId1);
                final int resourceId2 = resources.getIdentifier(
                        timeStatus+
                                threeHourForecast.getThreeHourWeatherArray().get(15).getWeatherArray().get(0).getIcon(),
                        "drawable", getApplicationContext().getPackageName());
                imageView2.setImageResource(resourceId2);
                final int resourceId3 = resources.getIdentifier(
                        timeStatus+
                                threeHourForecast.getThreeHourWeatherArray().get(23).getWeatherArray().get(0).getIcon(),
                        "drawable", getApplicationContext().getPackageName());
                imageView3.setImageResource(resourceId3);
                final int resourceId4 = resources.getIdentifier(
                        timeStatus+
                                threeHourForecast.getThreeHourWeatherArray().get(31).getWeatherArray().get(0).getIcon(),
                        "drawable", getApplicationContext().getPackageName());
                imageView4.setImageResource(resourceId4);
                final int resourceId = resources.getIdentifier(
                        timeStatus+
                                threeHourForecast.getThreeHourWeatherArray().get(38).getWeatherArray().get(0).getIcon(),
                        "drawable", getApplicationContext().getPackageName());
                imageView5.setImageResource(resourceId);

            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.v(TAG, throwable.getMessage());
            }
        });
    }

    private void setCurrentWeatherInfo(String cityName, Double temp, String description,String icon) {
        mCollapsingToolbarLayout.setTitle(cityName+" "+String.valueOf(temp)+"°C");
       // Log.i("Image_id",description.replaceAll(" ","_"));
        //Log.i("Time",String.valueOf(currentTime.getHours()));
        int currentTime = Calendar.getInstance().getTime().getHours();
        String timeStatus;
        if (currentTime>20||currentTime<6){
            timeStatus = "n";
        }
        else{
            timeStatus = "d";
        }

        Resources resources = getApplicationContext().getResources();
        final int resourceId = resources.getIdentifier(
                timeStatus+icon,
                "drawable", getApplicationContext().getPackageName());
        weather_image_status.setImageResource(resourceId);

        description = description.substring(0,1).toUpperCase() + description.substring(1);
        weather_description.setText(description);
        setWeekdays();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            sendNotification();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    int notificationId = 0;
    void sendNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Weather updated")
                        .setContentText(weather_description.getText().toString());
        Intent resultIntent = new Intent(this, ScrollingActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ScrollingActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    void setWeekdays(){
        Calendar sCalendar = Calendar.getInstance();
        int dayIndex=0;
        String weekday = new DateFormatSymbols().getWeekdays()[1];
        Log.i("day",weekday);
        String dayLongName = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        String a = "a";
        for(int i=1;i<8;i++){
            String day = new DateFormatSymbols().getWeekdays()[i];
            int b = dayLongName.compareTo(day);
            if(b==0){
                dayIndex = i;
            }
        }
        dayIndex++;
        weekday2.setText(new DateFormatSymbols().getWeekdays()[dayIndex%7+1]);
        dayIndex++;
        weekday3.setText(new DateFormatSymbols().getWeekdays()[dayIndex%7+1]);
        dayIndex++;
        weekday4.setText(new DateFormatSymbols().getWeekdays()[dayIndex%7+1]);
        dayIndex++;
        weekday5.setText(new DateFormatSymbols().getWeekdays()[dayIndex%7+1]);

    }

}
