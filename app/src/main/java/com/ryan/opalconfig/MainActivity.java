package com.ryan.opalconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

// /sdcard/android/data/com.spotify.music/files/spotifycache/users
// Here you’ll see all the usernames you've logged in with on this device.


public class MainActivity extends AppCompatActivity {
    //INTENT_EXTRA is for passing values to different activities
    public static final String INTENT_EXTRA = "com.example.opalconfig.INTENT_EXTRA";

    //constants

    String TEST_SERVER = "http://10.42.0.253/";
    String PLACEHOLDER_SERVER_URL = "https://jsonplaceholder.typicode.com/";
    String OPAL_SERVER_URL = "http://192.168.4.1/";
    String server_url = TEST_SERVER;

    String SSID = "MatsyaAP";
    String PSK = "MatsyaAP";

    //Classes
    boolean isScanning;
    //--Custom classes
    //manager class which will handle and store android user, and also tokens. will house all api methods (Maybe??)
    AuthenticationManager am = new AuthenticationManager();
    //--Standard Classes
    WifiManager wifiManager;


    //UI Elements -------------
    //text views
    TextView resultTextView;
    //checked text views
    CheckBox postCheckedTextView;
    CheckBox getCheckBox;
    //--input text boxes
    EditText usernameTextBox;
    EditText usernameValueTextBox;
    EditText passwordTextBox;
    EditText passwordValueTextBox;
    EditText urlTextBox;
    //--buttons
    Button submitButton;
    Button webViewButton;
    Button rebootButton;

//------functions

//-----
    private String getConnectedSSID(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        Log.d("WIFI", "connected to " + ssid);
        return ssid;
    }


private void goToServer () throws InterruptedException {
    if (checkIfSSIDAvailable(SSID)) {
        connectToWifi(SSID, PSK, wifiManager);
    } else {
//        wifiManager.setWifiEnabled(true);
////        wifiManager.disconnect();
////        wifiManager.reconnect();
    }


    //create new thread and change the define the run function
    Thread t = new Thread() {
        @Override
        public void run() {
            //checks for wifi, then waits 1 second.
            while (!checkForWiFi()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //if wifi return connected state, open webview to server page. if not HTTPS then it will open in chrome
            if (checkForWiFi()) {
                Log.d("WIFI", getConnectedSSID());
                if(getConnectedSSID().equals("\"" + SSID + "\"")){
                    Log.d("WIFI" , getConnectedSSID());
                    server_url = OPAL_SERVER_URL;
                }
                else
                {
                    server_url = TEST_SERVER;
                }
                Log.d("WIFI", server_url);

                openURL(server_url);

            }
        }
    };
    //start the process of the thread
    t.start();
}

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            // This condition is not necessary if you listen to only one action
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                isScanning = false;
                // Do what you want
            }
        }
    };






//this doesn't work yet
private boolean checkIfSSIDAvailable(String ssid) throws InterruptedException {
    isScanning = true;
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    registerReceiver(wifiScanReceiver, intentFilter);
    wifiManager.startScan();
    while (isScanning) {
        Thread.sleep(1500);
    }
    List<ScanResult> results = wifiManager.getScanResults();
    for (ScanResult scans : results) {
        Log.d("WIFI", scans.SSID);
        if (scans.SSID.equals(SSID)) {
            Log.d("WIFI", scans.SSID + "matches");
            return true;
        }
    }

    return false;
}

 //only adds network to devices saved network configuration
    private int addNetworkToSavedNetworks(String ssid, String password, WifiManager wifi){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        //must go between quotes
        wifiConfiguration.SSID = "\"" + ssid + "\"";
        wifiConfiguration.preSharedKey = "\"" + password + "\"";
//----get the network id as int and add to device wifi config
        return wifi.addNetwork(wifiConfiguration);
    }
//connects handset to specified wifi
    private void connectToWifi(String networkSSID, String networkPassword,WifiManager wifi){
        wifi.enableNetwork(addNetworkToSavedNetworks(networkSSID,networkPassword,wifiManager), true);
        Log.d("WIFI", "connecting to " + networkSSID);
        //enable wifi if it's not on
        if(!wifi.isWifiEnabled()) {
            Log.d("WIFI", "wifi not turned on, turning on now");
            wifi.setWifiEnabled(true);
            wifi.reconnect();
       }
       //lock connection for non-internet i think?
       wifi.createWifiLock("WifiConnection");
    }//end connectToWifi

//goes to webview activity at the specified URL sent in the INTENT_EXTRA
    private void openURL(String url){
        Intent intent = new Intent(this, Webview.class);
        intent.putExtra(INTENT_EXTRA, url);
        startActivity(intent);
    }//end setURL

    private void handleSubmitButton() throws JSONException, InterruptedException {
    //order of put doesn't matter
        String ssid = usernameValueTextBox.getText().toString();
        String usernameKey = usernameTextBox.getText().toString();
        String password = passwordValueTextBox.getText().toString();
        String passwordKey = passwordTextBox.getText().toString();
        JSONObject data = new JSONObject();
        //isEmpty check to prevent sending blank data. Used on key only so reset value is possible
        if(!usernameKey.isEmpty()){
            data.put(usernameKey, ssid);
        }
        if(!passwordKey.isEmpty() ){
            data.put(passwordKey, password);

        }
        if(postCheckedTextView.isChecked()){
            am.sendPostRequest(MainActivity.this, server_url+urlTextBox.getText().toString(),data, resultTextView);

        }
        if(getCheckBox.isChecked()){
            am.sendGetRequest(MainActivity.this,server_url+urlTextBox.getText().toString(),resultTextView );

        }

    };//end handleSubmit

    private void handleWebViewButtonClick() throws InterruptedException {

                goToServer();


        }    //end handleButton

    private boolean checkForWiFi() {
      ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
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
        //TextView Elements
        resultTextView = findViewById(R.id.resultTextView);
        //checkedTextView
        postCheckedTextView = findViewById(R.id.postCheckedTextView);
        getCheckBox = findViewById(R.id.getCheckBox);

            //user input text boxes
        usernameTextBox = findViewById(R.id.usernameTextBox);
        usernameValueTextBox = findViewById(R.id.usernameValueTextbox);
        passwordTextBox = findViewById(R.id.passwordTextBox);
        passwordValueTextBox = findViewById(R.id.passwordValueTextbox);
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
                       try {
                           handleWebViewButtonClick();
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
               });
               t.start();
           }
       });
        rebootButton = findViewById(R.id.rebootButton);
        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                try {
                    data.put("ssid","");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    data.put("password", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    am.sendPostRequest(MainActivity.this, server_url+"wifi/connect",data, resultTextView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }//end onCreate
}//endClass