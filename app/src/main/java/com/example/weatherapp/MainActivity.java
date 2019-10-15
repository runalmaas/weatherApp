package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private LatLng latandlng;

    public JSONObject jsonWeather; // the json that represents weather for the given location

    TextView temperatureText;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        //set cool background color(BLUE, ORANGE, GREEN etc.)
        Random r = new Random();
        int c = r.nextInt(3);

        View root = findViewById(R.id.layout1).getRootView();

        if(c == 0){
            setTheme(R.style.AppThemeBlue);
            root.setBackgroundColor(0xFF2196F3);
            //Default statusbarcolor is blue
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


        final EditText editText = findViewById(R.id.editText); //The edit field of the application
        temperatureText = findViewById(R.id.textView); // text field that displays temperature

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL)){
                    LatLng location = getLocationFromAddress(getApplicationContext(), editText.getText().toString());

                    if(location != null){
                        temperatureText.setText("*");
                        latandlng = new LatLng(location.latitude, location.longitude);
                    }else{
                        temperatureText.setText("Not location");
                        return true;
                    }

                    getJson(latandlng); // gets json for current city(//TODO this works poorly)

                    return true;
                }
                return false;
            }
        });
    }

    public void updateTextFields(TextView temperatureText){

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
     * This method needs to be called before anything can be done
     * it fetches the current weather forecast from api.met.no
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
                        updateTextFields(temperatureText);
                    }
                }

            }.execute(url);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    /**
     * return the current temperature outside from the given location in editText
     * @param json
     * @return
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

    public void setJsonWeather(JSONObject jsonObject){
        this.jsonWeather = jsonObject;
    }

    public JSONObject getJsonWeather(){
        return this.jsonWeather;
    }
}
