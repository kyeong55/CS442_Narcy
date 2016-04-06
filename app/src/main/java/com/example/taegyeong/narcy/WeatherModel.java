package com.example.taegyeong.narcy;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by taegyeong on 16. 3. 17..
 */
class Weather {
    String temperature;
    String temperatureMax;
    String temperatureMin;
    double precipitation;
    String humidity;
    String condition;
    String time;
    String precipProbability;
    String icon;

    public void setTemp(int t){ this.temperature = t+"°";}//kelvinToC(t);}
    public void setTempMax(int t){ this.temperatureMax = t+"°";}//kelvinToC(t);}
    public void setTempMin(int t){ this.temperatureMin = t+"°";}//kelvinToC(t);}
    public void setPrecipitation(double precipitation){this.precipitation = precipitation;}
    public void setPrecipProbability(int precipProbability){ this.precipProbability = precipProbability +"%";}
    public void setHumidity(String humidity){this.humidity = humidity;}
    public void setCondition(String condition){
        this.condition = condition;}
    public void setIcon(String icon){
        if (icon.indexOf("chance")>=0)
            this.icon = icon.substring(6);
        else
            this.icon = icon;
    }
    public void setTime(String time){ this.time = time;}

    public String getTemp() { return temperature;}
    public String getTempMax() { return temperatureMax;}
    public String getTempMin() { return temperatureMin;}
    public String getPrecipitation() { return precipitation+"mm";}
    public String getPrecipProbability() {return precipProbability;}
    public String getHumidity() { return humidity;}
    public String getCondition() { return condition;}
    public String getTime() { return time; }
    public String getIcon() { return icon; }
}

public class WeatherModel {

    Context context;

    final static String wundergroundURL = "http://api.wunderground.com/api/62d617e151b2bd8c/";

    public Weather currentWeather;
    public ArrayList<Weather> hourlyWeather;
    public ArrayList<Weather> forecastWeather;

    public String cityName;
    public String date;

    public WeatherModel(Context context){
        this.context = context;
    }

    public void getWeather(double lat,double lon){

        String geo = "/q/"+lat+","+lon+".json";
        String currentURLString = wundergroundURL + "conditions"+geo;
        String hourlyURLString = wundergroundURL + "hourly"+geo;
        String forecastURLString = wundergroundURL + "forecast"+geo;

        cityName = getLocation(lat,lon);

        currentWeather = new Weather();
        hourlyWeather = new ArrayList<>();
        forecastWeather = new ArrayList<>();

        try {
            URL currentURL = new URL(currentURLString);
            URL hourlyURL = new URL(hourlyURLString);
            URL forecastURL = new URL(forecastURLString);

            Log.d("debugging","URL: "+currentURLString);
            Log.d("debugging","URL: "+hourlyURLString);
            Log.d("debugging","URL: "+forecastURLString);

            HttpURLConnection currentConnection = (HttpURLConnection) currentURL.openConnection();
            InputStream currentInput = new BufferedInputStream(currentConnection.getInputStream());
            JSONObject currentJson = new JSONObject(getStringFromInputStream(currentInput));

            HttpURLConnection hourlyConnection = (HttpURLConnection) hourlyURL.openConnection();
            InputStream hourlyInput = new BufferedInputStream(hourlyConnection.getInputStream());
            JSONObject hourlyJson = new JSONObject(getStringFromInputStream(hourlyInput));

            HttpURLConnection forecastConnection = (HttpURLConnection) forecastURL.openConnection();
            InputStream forecastInput = new BufferedInputStream(forecastConnection.getInputStream());
            JSONObject forecastJson = new JSONObject(getStringFromInputStream(forecastInput));

            parseCurrentJSON(currentJson);
            Log.d("debugging", "current check");
            parseHourlyJSON(hourlyJson);
            Log.d("debugging", "hourly check");
            parseForecastJSON(forecastJson);
            Log.d("debugging", "forecast check");

        }catch(MalformedURLException e){
            Log.d("debugging", "Malformed URL");
            e.printStackTrace();
            return ;

        }catch(JSONException e) {
            Log.d("debugging", "JSON parsing error");
            e.printStackTrace();
            return ;
        }catch(IOException e){
            Log.d("debugging","URL Connection failed");
            e.printStackTrace();
            return ;
        }
    }

    private void parseCurrentJSON(JSONObject json) throws JSONException {
        JSONObject jsonOb = json.getJSONObject("current_observation");
        currentWeather.setTemp(jsonOb.getInt("temp_c"));
        currentWeather.setPrecipitation(jsonOb.getDouble("precip_today_metric"));
        currentWeather.setHumidity(jsonOb.getString("relative_humidity"));
        currentWeather.setCondition(jsonOb.getString("weather"));
        currentWeather.setIcon(jsonOb.getString("icon"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE MMM dd", Locale.ENGLISH);
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
        date = simpleDateFormat.format(new Date(jsonOb.getLong("local_epoch") * 1000));
        currentWeather.setTime(simpleTimeFormat.format(new Date(jsonOb.getLong("local_epoch") * 1000)));
    }

    private void parseHourlyJSON(JSONObject json) throws  JSONException {
        JSONArray jsonList = json.getJSONArray("hourly_forecast");
        JSONObject jsonOb;
        int hour;
        int dayCount=0;
        for (int i=0;i<jsonList.length();i++){
            Weather w = new Weather();
            jsonOb = jsonList.getJSONObject(i);
            hour = jsonOb.getJSONObject("FCTTIME").getInt("hour");
            if (i == 0)
                w.setTime("오늘 "+hour+"시");
            else if ((hour == 0)&&(dayCount == 0)){
                w.setTime("내일 "+hour+"시");
                dayCount++;
            }
            else if ((hour == 0)&&(dayCount == 1)){
                w.setTime("모레 "+hour+"시");
                dayCount++;
            }
            else
                w.setTime(hour+"시");
            w.setTemp(jsonOb.getJSONObject("temp").getInt("metric"));
            w.setCondition(jsonOb.getString("condition"));
            w.setIcon(jsonOb.getString("icon"));
            w.setPrecipProbability(jsonOb.getInt("pop"));
            hourlyWeather.add(w);
        }
    }

    private void parseForecastJSON(JSONObject json) throws JSONException {
        JSONArray jsonList = json.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");
        JSONObject jsonOb;
        for (int i=0;i<jsonList.length();i++){
            Weather w = new Weather();
            jsonOb = jsonList.getJSONObject(i);
            w.setTime(jsonOb.getJSONObject("date").getString("weekday_short"));
            w.setTempMax(jsonOb.getJSONObject("high").getInt("celsius"));
            w.setTempMin(jsonOb.getJSONObject("low").getInt("celsius"));

            if (i == 0){
                currentWeather.setTempMax(jsonOb.getJSONObject("high").getInt("celsius"));
                currentWeather.setTempMin(jsonOb.getJSONObject("low").getInt("celsius"));
            }
            w.setCondition(jsonOb.getString("conditions"));
            w.setIcon(jsonOb.getString("icon"));
            w.setPrecipitation(jsonOb.getInt("pop"));
            forecastWeather.add(w);
        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return stringBuilder.toString();
    }
    public String getLocation(double lat, double lng){
        String location = null;
        Geocoder gc = new Geocoder(context, Locale.KOREAN);
        try {
            List<Address> addresses = gc.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.d("debugging",address.getCountryName()+"");
                Log.d("debugging",address.getAdminArea()+"");
                Log.d("debugging",address.getThoroughfare()+"");
                Log.d("debugging", address.getPostalCode()+"");
                Log.d("debugging",address.getLocality()+"");
                Log.d("debugging",address.getFeatureName()+"");
                location = address.getAdminArea();
                if (location == null)
                    location = address.getLocality();
                else if (location.indexOf("광역시") > 0)
                    location = location.substring(0,location.indexOf("광역시"));
                else if (location.indexOf("특별시") > 0)
                    location = location.substring(0,location.indexOf("특별시"));
                else if (location.indexOf("시") > 0)
                    location = location.substring(0,location.indexOf("시"));
                else
                    location += " " + address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }
}
