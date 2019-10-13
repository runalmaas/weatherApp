package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

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

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = findViewById(R.id.editText); //The edit field of the application
        final TextView temperatureText = findViewById(R.id.textView); // text field that displays temperature

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                LatLng latandlng;  //(latitude, longitude)
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)){
                    LatLng location = getLocationFromAddress(getApplicationContext(), editText.getText().toString());
                    if(location != null){
                        latandlng = new LatLng(location.latitude, location.longitude);
                    }else{
                        // Default Bergen
                        location = getLocationFromAddress(getApplicationContext(), "Bergen");
                        latandlng = new LatLng(location.latitude, location.longitude);
                    }
                    try{
                        String from = String.format(Locale.US, "https://api.met.no/weatherapi/locationforecast/1.9/.json?lat=%f&lon=%f&msl=%d", latandlng.latitude, latandlng.longitude, 1);
                        URL url = new URL(from);

                        new AsyncTask<URL, Void, JSONObject>() {
                            JSONObject weather;
                            @Override
                            protected JSONObject doInBackground(URL... urls) {
                                URLConnection connection;
                                BufferedReader reader;
                                String line;
                                StringBuilder responseContent = new StringBuilder();
                                try{
                                    URL url = urls[0];
                                    connection = url.openConnection();

                                    //System.out.println(status);

                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    while((line = reader.readLine()) != null){
                                        responseContent.append(line);
                                    }
                                    reader.close();

                                    // System.out.println(responseContent.toString());
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
                                try{
                                    weather = jsonData;

                                    if(weather != null){
                                        temperatureText.setText(weather.get("created").toString());
                                    }else{
                                        temperatureText.setText("*");
                                    }
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }

                        }.execute(url);
                    }catch (MalformedURLException e){
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
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
     * return the current temperature outside from the given location in editText
     * @param json
     * @return
     */
    public String getCurrentTemp(JSONObject json){
        //TODO
        return null;
    }
}
