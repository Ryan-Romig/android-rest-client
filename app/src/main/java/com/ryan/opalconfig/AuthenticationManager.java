package com.ryan.opalconfig;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
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
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG + " GET ERROR", error.toString());
            }
        });


        queue.add(stringRequest);
    }//end sendGetRequest


    public void sendPostRequest(Context context, String url, String jsonInput){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG + "POST", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG + " ResponseError : ", error.toString());
            }
        }){

            //getParams is for sending parameters in POST request
            @Override
            protected Map<String,String> getParams(){
                //Must return HashMap
                HashMap<String,String> parameters = new HashMap<String,String>();
                //turn the string into a JSON so we can iterate through the keys and add the key-value pair
                //to to the parameters of the POST request
                try {
                    JSONObject stringToJSON = new JSONObject(jsonInput);
                    Iterator<?> objectKeys = stringToJSON.keys();
                    //iterates through the keys and adds the key-value pair to the parameter HashMap
                    while (objectKeys.hasNext()){
                        try {
                            String key = (String)objectKeys.next();
                            String value = stringToJSON.getString((key));
                            parameters.put(key,value);
                            Log.i(TAG,parameters.toString());

                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            Log.e(TAG, "Failed to convert and upload to parameters");
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "FAILED TO SEND PARAMETERS");
                }
                //just gets the keys of the JSON

                return parameters;
            }//end getParams
        };//end stringRequest inline declaration?? idk words

        //adjust timeout to prevent server timeout error
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    //--------finally, send the POST request
        queue.add(stringRequest);
    }//end postRequest




    AuthenticationManager (){

    }
}
