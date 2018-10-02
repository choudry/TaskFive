package com.example.ch_m_usman.taskfive;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Functions {

    /*******************************Hospital Functions***********************************/


    public static ArrayList<ArrayList<String>> hospitalJson(String response) throws IOException {
        ArrayList<ArrayList<String>> resultList = new ArrayList<>();
        ArrayList<String> latList = new ArrayList<>();
        ArrayList<String> lngList = new ArrayList<>();
        ArrayList<String> vacinity = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();



        try {
            JSONObject responeObject = new JSONObject(response);
            JSONArray result_array = responeObject.getJSONArray("results");
            int length = result_array.length();
            for (int i = 0;i<length;i++){
                JSONObject root = result_array.getJSONObject(i);
                JSONObject geometry = root.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                latList.add(location.getString("lat"));
                lngList.add(location.getString("lng"));
                vacinity.add(root.getString("vicinity"));
                name.add(root.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        resultList.add(latList);
        resultList.add(lngList);
        resultList.add(vacinity);
        resultList.add(name);
        return resultList;
    }
}
