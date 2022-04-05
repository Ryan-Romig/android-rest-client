package com.ryan.opalconfig;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA = "com.example.opalconfig.INTENT_EXTRA";
    String SERVER_URL = "http://192.168.4.1/plugins/" ;
    String SSID = "MatsyaAP";
    String PSK = "MatsyaAP";
    WifiManager wifiManager;
    EditText usernameTextBox;
    EditText passwordTextBox;
    Button submitButton;

    public void sendAPIRequest(Context context, String url, String method, String jsonExtra) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);//create a new request queue , then send the queue

        //StringRequest object takes 4 parameters, (1.Method (get or post), 2.the API server URL, 3.and a Response Listener, 4. error Listener)

        if (method.toUpperCase() == "GET") {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    SERVER_URL + url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("VOLLEY", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                        }

                    });
            queue.add(stringRequest);


        } else {

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    SERVER_URL + url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("VOLLEY", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                        }

                    }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("username", usernameTextBox.getText().toString());
                    params.put("password", passwordTextBox.getText().toString());
                    return params;
                }
            };
            queue.add(stringRequest);
        }


    }

    private void connectToOpalHotspot(String networkSSID, String networkPassword,WifiManager wifi){

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + networkPassword + "\"";
        int netID = wifiManager.addNetwork(wifiConfiguration);
        wifi.setWifiEnabled(true);
//        wifi.disconnect();
        wifi.enableNetwork(netID, true);
        wifi.reconnect();
    }

    private void setURL(String url){
        Intent intent = new Intent(this, Webview.class);
        intent.putExtra(INTENT_EXTRA, url);
        startActivity(intent);

    }
    private String getConnectedSSID(){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameTextBox = findViewById(R.id.usernameTextBox);
        passwordTextBox = findViewById(R.id.usernameTextBox);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    sendAPIRequest(MainActivity.this, "/wifi/connect","post","" );
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                try {
                    sendAPIRequest(MainActivity.this,"", "get", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(getConnectedSSID() == "MatsyaAP"){
            setURL(SERVER_URL);
        }
        else {
            connectToOpalHotspot(SSID,PSK,wifiManager);
        }





        //use WifiManager class to collect saved wifi credentials
//        List<WifiConfiguration> savedNetworks = (List<WifiConfiguration>)wifiManager.getConfiguredNetworks();
//        Log.i("WIFI", savedNetworks.toString());

        //send RESTful post request to the opal API to upload wifi creds

    }
}