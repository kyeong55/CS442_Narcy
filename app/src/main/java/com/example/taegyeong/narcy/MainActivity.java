package com.example.taegyeong.narcy;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location; 
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mySwipeRefreshLayout;

    private TextView cityName;
    private TextView date;
    private TextView temperature;
    private TextView tempHigh;
    private TextView tempLow;
    private TextView humidity;
    private TextView precip;
    private TextView description;
    private TextView lastUpdate;
    private VideoView videoHolder;
    private ImageView infoButton;
    private View logoView;
    private List<ForecastHolder> forecastHolders;

    private Uri video;

    private LocationManager manager;
    private HourlyForecastAdapter hourlyForecastAdapter;

    final private int NETWORK_ERROR = 1;
    final private int GPS_ERROR = 2;

    private boolean isLogo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cityName = (TextView) findViewById(R.id.city_name);
        date = (TextView) findViewById(R.id.date);
        temperature = (TextView) findViewById(R.id.temperature);
        tempHigh = (TextView) findViewById(R.id.temp_high);
        tempLow = (TextView) findViewById(R.id.temp_low);
        humidity = (TextView) findViewById(R.id.humidity);
        precip = (TextView) findViewById(R.id.precip);
        description = (TextView) findViewById(R.id.description);
        lastUpdate = (TextView) findViewById(R.id.last_update);
        videoHolder = (VideoView) findViewById(R.id.video);
        infoButton = (ImageView) findViewById(R.id.info_button);
        logoView = findViewById(R.id.logo);

        forecastHolders = new ArrayList<>();
        for (int i=0;i<4;i++){
            forecastHolders.add(new ForecastHolder(i));
        }

        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        getLocation();
                    }
                }
        );
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        RecyclerView hourlyForecastView = (RecyclerView) findViewById(R.id.forecast_view);
        hourlyForecastView.setHasFixedSize(true);
        hourlyForecastView.setLayoutManager(layoutManager);

        hourlyForecastAdapter = new HourlyForecastAdapter(getApplicationContext());
        hourlyForecastView.setAdapter(hourlyForecastAdapter);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
            }
        });

        getLocation();
    }

    private void showInfoDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("[CS442] Android Programming HW#1\n\n" +
                "20130505 이태경(Lee Taegyeong)\n\n\n" +
                "Refresh: Swipe down\n\n\n" +
                "Weather API: Weather Underground\n" +
                "\t(api.wunderground.com)")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog infoDialog = dialogBuilder.create();
        infoDialog.setTitle("Narcy");
        infoDialog.setIcon(R.mipmap.ic_launcher);
        infoDialog.show();
    }

    public void setView(WeatherModel weatherModel){
        playVideo(weatherModel.currentWeather.icon);
        cityName.setText(weatherModel.cityName);
        date.setText(weatherModel.date);
        temperature.setText(weatherModel.currentWeather.getTemp());
        tempHigh.setText(weatherModel.currentWeather.getTempMax());
        tempLow.setText(weatherModel.currentWeather.getTempMin());
        humidity.setText(weatherModel.currentWeather.getHumidity());
        precip.setText(weatherModel.currentWeather.getPrecipitation());
        description.setText(weatherModel.currentWeather.getCondition());
        lastUpdate.setText("Last Update: " + weatherModel.currentWeather.getTime());
        for (int i=0;i<4;i++){
            forecastHolders.get(i).date.setText(weatherModel.forecastWeather.get(i).getTime());
            forecastHolders.get(i).tempHigh.setText(weatherModel.forecastWeather.get(i).getTempMax());
            forecastHolders.get(i).tempLow.setText(weatherModel.forecastWeather.get(i).getTempMin());
            forecastHolders.get(i).icon.setImageDrawable(getIconDrawable(weatherModel.forecastWeather.get(i).getIcon()));
        }
        hourlyForecastAdapter.updateItems(weatherModel.hourlyWeather);
        if(isLogo) {
            new VideoTask().execute(humidity);
            isLogo = false;
        }
    }
    public void playVideo(String icon) {
        if (icon.contentEquals("clear")||icon.contentEquals("sunny"))
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sunny_video);
        else if (icon.contentEquals("cloudy"))
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cloud_video);
        else if (icon.indexOf("partly")>=0||icon.indexOf("mostly")>=0)
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sunnycloud_video);
        else if (icon.contentEquals("fog")||icon.contentEquals("hazy"))
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mist_video);
        else if (icon.contentEquals("rain"))
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rain_video);
        else if (icon.contentEquals("tstorms"))
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.thunder_video);
        else if (icon.contentEquals("snow")||icon.contentEquals("sleet")||icon.contentEquals("flurries"))
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.snow_video);
        else
            Log.d("debugging", "Icon Error!");
        videoHolder.setVideoURI(video);
        videoHolder.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoHolder.start();
            }
        });
        videoHolder.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                videoHolder.start();
            }
        });
    }

    public class ForecastHolder{
        TextView date;
        TextView tempHigh;
        TextView tempLow;
        ImageView icon;
        public ForecastHolder(int index){
            switch (index){
                case 0:
                    date = (TextView) findViewById(R.id.forecast_day_1);
                    tempHigh = (TextView) findViewById(R.id.forecast_temp_high_1);
                    tempLow = (TextView) findViewById(R.id.forecast_temp_low_1);
                    icon = (ImageView) findViewById(R.id.forecast_icon_1);
                    break;
                case 1:
                    date = (TextView) findViewById(R.id.forecast_day_2);
                    tempHigh = (TextView) findViewById(R.id.forecast_temp_high_2);
                    tempLow = (TextView) findViewById(R.id.forecast_temp_low_2);
                    icon = (ImageView) findViewById(R.id.forecast_icon_2);
                    break;
                case 2:
                    date = (TextView) findViewById(R.id.forecast_day_3);
                    tempHigh = (TextView) findViewById(R.id.forecast_temp_high_3);
                    tempLow = (TextView) findViewById(R.id.forecast_temp_low_3);
                    icon = (ImageView) findViewById(R.id.forecast_icon_3);
                    break;
                case 3:
                    date = (TextView) findViewById(R.id.forecast_day_4);
                    tempHigh = (TextView) findViewById(R.id.forecast_temp_high_4);
                    tempLow = (TextView) findViewById(R.id.forecast_temp_low_4);
                    icon = (ImageView) findViewById(R.id.forecast_icon_4);
                    break;
            }
        }
    }

    public Drawable getIconDrawable(String icon){
        if (icon.contentEquals("clear")||icon.contentEquals("sunny"))
            return getResources().getDrawable(R.drawable.sunny);
        else if (icon.contentEquals("cloudy"))
            return getResources().getDrawable(R.drawable.cloud);
        else if (icon.indexOf("partly")>=0||icon.indexOf("mostly")>=0)
            return getResources().getDrawable(R.drawable.sunnycloud);
        else if (icon.contentEquals("fog")||icon.contentEquals("hazy"))
            return getResources().getDrawable(R.drawable.mist);
        else if (icon.contentEquals("rain"))
            return getResources().getDrawable(R.drawable.rain);
        else if (icon.contentEquals("tstorms"))
            return getResources().getDrawable(R.drawable.thunder);
        else if (icon.contentEquals("snow")||icon.contentEquals("sleet")||icon.contentEquals("flurries"))
            return getResources().getDrawable(R.drawable.snow);
        else
            return null;
    }

    public void logoDisappear(){
        if (logoView.getVisibility() == View.VISIBLE) {
            logoView.setVisibility(View.GONE);
        }
    }

    public void handleException(int error){
        if(isLogo){
            final Button retryButton = (Button)findViewById(R.id.retry_button);
            final ProgressBar logoProgressbar= (ProgressBar)findViewById(R.id.logo_progressbar);
            retryButton.setVisibility(View.VISIBLE);
            logoProgressbar.setVisibility(View.INVISIBLE);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryButton.setVisibility(View.INVISIBLE);
                    logoProgressbar.setVisibility(View.VISIBLE);
                    getLocation();
                }
            });
        }
        else{
            mySwipeRefreshLayout.setRefreshing(false);
        }
        if(error == NETWORK_ERROR)
            Snackbar.make(logoView, "네트워크 연결을 확인한 후 다시 시도해주세요.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        else if(error == GPS_ERROR)
            Snackbar.make(logoView, "'위치설정'을 확인한 후 다시 시도해주세요.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
    }

    public void getWeather(double latitude, double longitude) {
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(latitude, longitude);
        Log.d("debugging", "execute!");
    }

    public class WeatherTask extends AsyncTask<Double, Void, WeatherModel> {
        @Override
        public WeatherModel doInBackground(Double... params) {
            WeatherModel model = new WeatherModel(getApplicationContext());

            double lat = params[0];
            double lon = params[1];
            // API 호출
            model.getWeather(lat, lon);

            return model;
        }

        @Override
        public void onPostExecute(WeatherModel model) {
            super.onPostExecute(model);
            setView(model);
            mySwipeRefreshLayout.setRefreshing(false);
        }
    }

    public class VideoTask extends AsyncTask<View, Void, View> {
        @Override
        public View doInBackground(View... params) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(View model) {
            super.onPostExecute(model);
            logoDisappear();
        }
    }

    private void getLocation() {
        if (manager == null)
            manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("debugging", "Permission denied");
            handleException(GPS_ERROR);
            return;
        }
        if (!(gps_enabled||network_enabled)){
            handleException(GPS_ERROR);
            return;
        }
        if (gps_enabled)
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if (network_enabled)
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNet);
        Log.d("debugging", "내 위치 요청");
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.removeUpdates(this);
            manager.removeUpdates(locationListenerNet);
            Log.d("debugging", "위치(gps): " + lat + ", " + lon);
            getWeather(lat, lon);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNet = new LocationListener() {
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.removeUpdates(this);
            manager.removeUpdates(locationListenerGps);
            Log.d("debugging", "위치(net): " + lat + ", " + lon);
            getWeather(lat,lon);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
}
