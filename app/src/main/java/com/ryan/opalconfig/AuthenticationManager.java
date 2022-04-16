package com.ryan.opalconfig;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationManager {

    public String serverResponse;
    public boolean complete = false;
    String TAG = "Authentication Manager";


    public void sendGetRequest(Context context, String url, TextView textView){
        Log.d(TAG, url);
        complete = false;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG + "GET RESPONSE", response);
                        serverResponse = response;
                        textView.setText(response);
                        complete = true;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG + " GET ERROR", error.toString());
                textView.setText(error.toString());
                complete = true;

            }
        });
       //.add() sends the request
        queue.add(stringRequest);
//
    }//end sendGetRequest


    public void sendPostRequest(Context context, String url, JSONObject jsonInput, TextView textView) throws JSONException {
               RequestQueue queue = Volley.newRequestQueue(context);


        Log.d(TAG, jsonInput.toString());


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonInput,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        Log.i(TAG, "POST RESPONSE "+ response.toString());
                        textView.setText(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                      Log.e(TAG, "POST RESPONSE ERROR "+ error.toString());
                        textView.setText(error.toString());
                    }
                });
//        //adjust timeout to prevent server timeout error
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    //--------finally, send the POST request

        queue.add(jsonObjReq);

    }//end postRequest




    AuthenticationManager (){
         serverResponse = "";

    }//end constructor

}//end class
