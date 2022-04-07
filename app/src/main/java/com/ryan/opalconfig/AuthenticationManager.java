package com.ryan.opalconfig;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthenticationManager {

    String TAG = "Authentication Manager";
    public void sendGetRequest(Context context, String url){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> Log.i(TAG +"GET", response),
                error -> Log.e(TAG, error.toString()));
        queue.add(stringRequest);
    }//end sendGetRequest

    public void sendPostRequest(Context context, String url, String jsonInput){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> Log.i(TAG + "POST", response),
                error -> Log.e(TAG, error.toString())){
            //getParams is for sending parameters in POST request
            @Override
            protected Map<String,String> getParams(){
                //Must return HashMap
                HashMap<String,String> parameters = new HashMap<String,String>();
                //turn the string into a JSON so we can iterate through the keys and add the key-value pair
                //to to the parameters of the POST request
                JSONObject stringToJSON = new JSONObject(parameters);
                //just gets the keys of the JSON
                Iterator<?> objectKeys = stringToJSON.keys();
                //iterates through the keys and adds the key-value pair to the parameter HashMap
                while (objectKeys.hasNext()){
                    try {
                        String key = (String)objectKeys.next();
                        String value = stringToJSON.getString((key));

                        parameters.put(key,value);

                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
                return parameters;
            }//end getParams
        };//end stringRequest inline declaration?? idk words

    //--------finally, send the POST request
        queue.add(stringRequest);
    }//end postRequest




    AuthenticationManager (){

    }
}
