package com.example.romankhrupa.coffeeweather;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kwabenaberko.openweathermaplib.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.sql.Array;
import java.sql.Time;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static java.security.AccessController.getContext;

public class ScrollingActivity extends AppCompatActivity {

    public final static String TAG = "ScrollingActivity";
    OpenWeatherMapHelper helper;
    CollapsingToolbarLayout  mCollapsingToolbarLayout;
    TextView weather_description;
    ImageView weather_image_status;
    //Sorry for hardcoding
//    TextView weekday2 = (TextView)findViewById(R.id.day2);
//    TextView weekday3 = (TextView)findViewById(R.id.day3);
//    TextView weekday4 = (TextView)findViewById(R.id.day4);
   // TextView weekday5 = (TextView)findViewById(R.id.day5);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Updated ^-^", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateInfo();
                getWeekday();
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
        //TODO: insert this location ->
        helper.getCurrentWeatherByGeoCoordinates(location.getLatitude(), location.getLongitude(), new OpenWeatherMapHelper.CurrentWeatherCallback() {
            @Override
            public void onSuccess(CurrentWeather currentWeather) {
                Log.e(TAG,
                        "Coordinates: " + currentWeather.getCoord().getLat() + ", "+currentWeather.getCoord().getLat() +"\n"
                                +"Weather Description: " + currentWeather.getWeatherArray().get(0).getDescription() + "\n"
                                +"Max Temperature: " + currentWeather.getMain().getTempMax()+"\n"
                                +"Wind Speed: " + currentWeather.getWind().getSpeed() + "\n"
                                +"City, Country: " + currentWeather.getName() + ", " + currentWeather.getSys().getCountry()
                );
                setCurrentWeatherInfo(currentWeather.getName(),
                        currentWeather.getMain().getTemp(),
                        currentWeather.getWeatherArray().get(0).getDescription(),
                        currentWeather.getWind().getSpeed());
                sendNotification();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.v(TAG, throwable.getMessage());
            }
        });
    }

    private void setCurrentWeatherInfo(String cityName, Double temp, String description,Double windSpeed) {
        mCollapsingToolbarLayout.setTitle(cityName+" "+String.valueOf(temp)+"°C");
       // Log.i("Image_id",description.replaceAll(" ","_"));
        //Log.i("Time",String.valueOf(currentTime.getHours()));
        int currentTime = Calendar.getInstance().getTime().getHours();
        String timeStatus;
        if (currentTime>20||currentTime<6){
            timeStatus = "night";
        }
        else{
            timeStatus = "day";
        }

        Resources resources = getApplicationContext().getResources();
        final int resourceId = resources.getIdentifier(
                timeStatus+"_"+
                description.replaceAll(" ", "_"),
                "drawable", getApplicationContext().getPackageName());
        weather_image_status.setImageResource(resourceId);
        description = description.substring(0,1).toUpperCase() + description.substring(1);
        weather_description.setText(description);

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
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    String getWeekday(){
        Calendar sCalendar = Calendar.getInstance();
        String weekday = new DateFormatSymbols().getWeekdays()[1];
        Log.i("day",weekday);
       // String dayLongName = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
       // String dayLongName = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());


//        Log.i("calendar",dayLongName);
//        //int currentTime = Calendar.getInstance().getTime().get;
//        DateFormatSymbols dfs = new DateFormatSymbols();
//        String[] weekdays = dfs.getWeekdays();
//        Log.i("day",weekdays[days]);
//        return weekdays[days];
        return "d";
    }

}
