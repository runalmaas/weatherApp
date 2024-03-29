package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import us.dustinj.timezonemap.TimeZoneMap;

public class MainActivity extends AppCompatActivity {

    private LatLng latandlng;

    public JSONObject jsonWeather; // the json that represents weather for the given location

    TextView temperatureText;
    TextView latitude;
    TextView longitude;
    TextView timeAndDate;
    TimeZoneMap timeMap;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //set cool background color(BLUE, ORANGE, GREEN etc.)
        setRandomColor();
        // setup timezonemap in async task to make startup faster
        setTimeZoneMap();

        final EditText editText = findViewById(R.id.editText); //The edit field of the application
        temperatureText = findViewById(R.id.textView); // text field that displays temperature
        latitude = findViewById(R.id.Latitude); //Current latitude gets displayed here
        longitude = findViewById(R.id.Longitude); //Current longitude gets displayed here
        timeAndDate = findViewById(R.id.timeAndDate); //displays current local time for given location


        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL)){
                    LatLng location = getLocationFromAddress(getApplicationContext(), editText.getText().toString());

                    if(location != null){
                        temperatureText.setTextSize(50);
                        temperatureText.setText("Loading");
                        latandlng = new LatLng(location.latitude, location.longitude);
                    }else{
                        temperatureText.setTextSize(50);
                        temperatureText.setText("Invalid location");
                        latitude.setText("");
                        longitude.setText("");
                        timeAndDate.setText("");
                        return true;
                    }

                    getJson(latandlng); // gets json for current city(//TODO this works poorly)
                    return true;
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setRandomColor(){
        Random r = new Random();
        int c = r.nextInt(3);

        View root = findViewById(R.id.layout1).getRootView();

        if(c == 0){
            setTheme(R.style.AppThemeBlue);
            root.setBackgroundColor(0xFF2196F3);
            //Default statusbar color is blue
        }
        if(c == 1){
            setTheme(R.style.AppThemeGreen);
            root.setBackgroundColor(0xFF4CAF50);
            getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        }
        if(c == 2){
            setTheme(R.style.AppThemeOrange);
            root.setBackgroundColor(0xFFFF5722);
            getWindow().setStatusBarColor(getResources().getColor(R.color.orange));
        }
    }

    private void updateCurrentTempTextField(TextView temperatureText){

        if(getJsonWeather() != null){
            try {
                temperatureText.setText(getCurrentTemp(getJsonWeather()) + "°C");
                setJsonWeather(null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            temperatureText.setText("Null");
        }

        setJsonWeather(null);
    }
    /**
     * return latitude and longitude of the given address(city name)
     * @param context
     * @param strAddress
     * @return
     */
    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return p1;
    }

    /**
     * This takes some time to get
     * @param latLng
     * @return the local time at the given location
     */
    public String getTimeFromLocation(LatLng latLng){
        Instant instant;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            instant = Instant.now();
            ZoneId zoneId = ZoneId.of(timeMap.getOverlappingTimeZone(latLng.latitude, latLng.longitude).get().getZoneId());
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
            return zdt.toString();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private void setTimeZoneMap(){
        new AsyncTask<Void, Void, TimeZoneMap>() {

            @Override
            protected TimeZoneMap doInBackground(Void... voids) {
                TimeZoneMap timeMap = TimeZoneMap.forEverywhere();
                return timeMap;
            }

            @Override
            protected void onPostExecute(TimeZoneMap map) {
                timeMap = map;
            }
        }.execute();
    }
    /**
     * This method needs to be called before anything can be done
     * it fetches the current weather forecast from api.met.no and
     * fills in textviews with relevant information
     * @param latandlng
     */
    @SuppressLint("StaticFieldLeak")
    public void getJson(final LatLng latandlng){
        try{
            String from = String.format(Locale.US, "https://api.met.no/weatherapi/locationforecast/1.9/.json?lat=%f&lon=%f", latandlng.latitude, latandlng.longitude);
            URL url = new URL(from);

            new AsyncTask<URL, Void, JSONObject>() {

                @Override
                protected JSONObject doInBackground(URL... urls) {
                    URLConnection connection;
                    BufferedReader reader;
                    String line;
                    StringBuilder responseContent = new StringBuilder();
                    try{
                        URL url = urls[0];
                        connection = url.openConnection();

                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while((line = reader.readLine()) != null){
                            responseContent.append(line);
                        }
                        reader.close();

                        return new JSONObject(responseContent.toString());

                    }catch(MalformedURLException e){
                        return null;
                    }catch(IOException e){
                        return null;
                    }catch (JSONException e){
                        return null;
                    }
                }


                @Override
                protected void onPostExecute(JSONObject jsonData) {
                    if(jsonData == null){
                        getJson(latandlng);
                    }else{
                        setJsonWeather(jsonData);
                        updateCurrentTempTextField(temperatureText);
                        temperatureText.setTextSize(125);
                        latitude.setText("LAT: "+ round(latandlng.latitude, 4));
                        longitude.setText("LNG: "+ round(latandlng.longitude, 4));
                        timeAndDate.setText(getTimeFromLocation(latandlng));
                    }
                }

            }.execute(url);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param json
     * @return the current temperature from the given location in editText
     */
    public String getCurrentTemp(JSONObject json) throws JSONException {

        JSONArray arr = (JSONArray) json.getJSONObject("product").getJSONArray("time");

        for(int i = 0; i < arr.length(); i++){
            if(arr.getJSONObject(i).getJSONObject("location").has("temperature")){
                //TODO get weather at current time
                return arr.getJSONObject(i).getJSONObject("location").getJSONObject("temperature").getString("value");
            }
        }

        return null;
    }

    /**
     * helper to round longitude and latitude to specifiv decimal point
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public void setJsonWeather(JSONObject jsonObject){
        this.jsonWeather = jsonObject;
    }

    public JSONObject getJsonWeather(){
        return this.jsonWeather;
    }
}
