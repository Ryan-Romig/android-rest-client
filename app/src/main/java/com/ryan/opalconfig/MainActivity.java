package com.ryan.opalconfig;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA = "com.example.opalconfig.INTENT_EXTRA";
    String SERVER_URL = "http://192.168.137.1:3000/" ;
    String SSID = "WiFi";
    String PSK = "WifiPassword";
    WifiManager wifiManager;
    Button turnOnWifiButton;
    Button goToPageButton;

    private void connectToOpalHotspot(String networkSSID, String networkPassword,WifiManager wifi){

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + networkPassword + "\"";
        int netID = wifiManager.addNetwork(wifiConfiguration);
        wifi.setWifiEnabled(true);
        wifi.disconnect();
        wifi.enableNetwork(netID, true);
        wifi.reconnect();
    }

    private void setURL(String url){
        Intent intent = new Intent(this, Webview.class);
        intent.putExtra(INTENT_EXTRA, url);
        startActivity(intent);

    }
    private void connectToWifi(){
        connectToOpalHotspot(SSID,PSK, wifiManager);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        turnOnWifiButton = findViewById(R.id.turnOnWifiButton);
        goToPageButton = findViewById(R.id.loadPageButton);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        turnOnWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToOpalHotspot(SSID,PSK, wifiManager);

                if(wifiManager.isWifiEnabled() == true) {
                    turnOnWifiButton.setText("on");
                }
                else{
                    turnOnWifiButton.setText("off");
                }

            }
        });
        goToPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setURL(SERVER_URL);
            }
        });

        //use WifiManager class to collect saved wifi credentials
//        List<WifiConfiguration> savedNetworks = (List<WifiConfiguration>)wifiManager.getConfiguredNetworks();
//        Log.i("WIFI", savedNetworks.toString());

        //send RESTful post request to the opal API to upload wifi creds

    }
}