package com.example.weatherapp;


import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class JsonRetriever extends AsyncTask<String, Void, JSONObject> {
    public JSONObject json;

    public JsonRetriever(){

    }

    @Override
    protected JSONObject doInBackground(String... from) {
        URLConnection connection;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        try{
            URL url = new URL(from[0]);
            connection = url.openConnection();

            //System.out.println(status);

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null){
                responseContent.append(line);
            }
            reader.close();

            // System.out.println(responseContent.toString());
            return makeJsonObject(responseContent.toString());

        }catch(MalformedURLException e){
            return null;
        }catch(IOException e){
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject json){
        super.onPostExecute(json);
        if(json != null){
            this.json = json;
        }else{
            this.json = null;
        }
    }


    public static JSONObject makeJsonObject(String responseBody){
        try{
            return new JSONObject(responseBody);
        }catch (JSONException e){
            return null;
        }
    }
}
