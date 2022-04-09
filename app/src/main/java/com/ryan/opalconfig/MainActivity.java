package com.ryan.opalconfig;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

// /sdcard/android/data/com.spotify.music/files/spotifycache/users
// Here you’ll see all the usernames you've logged in with on this device.


public class MainActivity extends AppCompatActivity {
    //INTENT_EXTRA is for passing values to different activities
    public static final String INTENT_EXTRA = "com.example.opalconfig.INTENT_EXTRA";

    //constants

    String TEST_SERVER = "http://10.42.0.253/";
    String PLACEHOLDER_SERVER_URL = "https://jsonplaceholder.typicode.com/";
    String OPAL_SERVER_URL = "http://192.168.4.1/";
    String SERVER_URL = OPAL_SERVER_URL;

    String SSID = "MatsyaAP";
    String PSK = "MatsyaAP";

    //Classes
    //--Custom classes
    //manager class which will handle and store android user, and also tokens. will house all api methods (Maybe??)
    AuthenticationManager am = new AuthenticationManager();
    //--Standard Classes
    WifiManager wifiManager;


    //UI Elements -------------
    //--input text boxes
    EditText usernameTextBox;
    EditText usernameValueTextBox;
    EditText passwordTextBox;
    EditText passwordValueTextBox;
    EditText urlTextBox;
    //--buttons
    Button submitButton;
    Button webViewButton;
//------functions

//-----
private void goToServer () {
    String ssid = "MatsyaAP";
    String password = "MatsyaAP";
    connectToWifi(ssid, password, wifiManager);
    Thread t = new Thread(){
        @Override
        public void run(){
            while(!checkForWiFi()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(checkForWiFi()){
                openURL(SERVER_URL);            }
            else{

            }

        }
    };
    t.start();

}



    //used to auto connect to opal hotspot
    private void connectToWifi(String networkSSID, String networkPassword,WifiManager wifi){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + networkPassword + "\"";
        int netID = wifi.addNetwork(wifiConfiguration);
        wifi.enableNetwork(netID, true);
        //enable wifi if it's not on
        if(!wifi.isWifiEnabled()){
            wifi.setWifiEnabled(true);
        }


        //switch to MatsyaAP if its not already connected
            wifi.enableNetwork(netID, true);
            wifi.reconnect();
            //lock connection for non-internet i think?
       wifi.createWifiLock("WifiConnection");

    }//end connectToOpalHotspot

//goes to webview activity at the specified URL sent in the INTENT_EXTRA
    private void openURL(String url){
        Intent intent = new Intent(this, Webview.class);
        intent.putExtra(INTENT_EXTRA, url);
        startActivity(intent);
    }//end setURL

    private void handleSubmitButton() throws JSONException, InterruptedException {
//        am.sendGetRequest(MainActivity.this,SERVER_URL + "plugins/wifi");
        //(JSONObject.put alphabetical order?
        String ssid = usernameTextBox.getText().toString();
        String usernameKey = usernameTextBox.getText().toString();
        String password = passwordTextBox.getText().toString();
        String passwordKey = passwordTextBox.getText().toString();
        JSONObject data = new JSONObject();
        data.put(usernameKey, ssid);
        data.put(passwordKey, password);
        am.sendPostRequest(MainActivity.this, SERVER_URL+urlTextBox.getText().toString(),data);
        am.sendGetRequest(MainActivity.this,SERVER_URL+urlTextBox.getText().toString());
        //                configureWifi(usernameTextBox.getText().toString(),passwordTextBox.getText().toString());
    };//end handleSubmit

    private void handleWebViewButtonClick(){


            goToServer();
        }    //end handleButton

    private boolean checkForWiFi() {
      ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
      if(networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
          return true;
      }
      else{
      return false;
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setContentView(R.layout.activity_main);
        //UI element assignments
            //user input text boxes
        usernameTextBox = findViewById(R.id.usernameTextBox);
        passwordTextBox = findViewById(R.id.passwordTextBox);
        urlTextBox  = findViewById(R.id.urlTextBox);
            //buttons
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    handleSubmitButton();
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });//end onclickListener
       webViewButton = findViewById(R.id.webViewButton);
       webViewButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Thread t = new Thread(new Runnable() {
                   @Override
                   public void run() {
                       handleWebViewButtonClick();
                   }
               });
               t.start();
           }
       });


    }//end onCreate
}//endClass