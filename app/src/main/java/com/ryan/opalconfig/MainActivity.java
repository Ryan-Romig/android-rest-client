package com.ryan.opalconfig;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

//-------------------NOTES----------------------------------------//
// /sdcard/android/data/com.spotify.music/files/spotifycache/users
// Here you’ll see all the usernames you've logged in with on this device.


public class MainActivity extends AppCompatActivity {
    //INTENT_EXTRA is for passing URL value to Webview Activity
    public static final String INTENT_EXTRA = "com.example.opalconfig.INTENT_EXTRA";
private String TAG = "CONFIG";
    //constants
    String TEST_SERVER = "http://10.42.0.253/";
    String PLACEHOLDER_SERVER_URL = "https://jsonplaceholder.typicode.com/";
    String OPAL_SERVER_URL = "http://192.168.4.1/";
    String server_url = "";
    String url_extra = "";


    //----WiFi Configurations
    String SSID = "MatsyaAP";
    String PSK = "MatsyaAP";

    //variables
    boolean isScanning;
    boolean hasConnectionToPlayer;
    boolean isScanningNetwork;
    JSONObject parameterAsJSON;

    //--Custom classes
    //manager class which will handle and store android user, and also tokens. will house all api methods (Maybe??)
    AuthenticationManager am = new AuthenticationManager();
    //--Standard Classes
    WifiManager wifiManager;


    //UI Elements -------------
    //text views
    TextView parametersTextView;
    TextView responseTextView;

    //checked text views
    CheckBox postCheckedTextView;
    CheckBox getCheckBox;
    //--input text boxes
    EditText keyTextBox;
    EditText valueTextBox;
    EditText urlTextBox;
    EditText urlExtraTextBox;
    //--buttons
    Button submitButton;
    Button connectButton;
    Button rebootButton;
    Button addParameterButton;
    Button resetParametersButton;

    String textContainer = "";

//------------------------------------functions-----------------------------------//
    private List<WifiConfiguration> getSavedWifiConfigurations() {
        return wifiManager.getConfiguredNetworks();
    }
    //only adds network to devices saved network configuration
    private int addWifiToSavedWifiConfiguration(String ssid, String password) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        //must go between quotes
        wifiConfiguration.SSID = "\"" + ssid + "\"";
        wifiConfiguration.preSharedKey = "\"" + password + "\"";
//----get the network id as int and add to device wifi config
        return wifiManager.addNetwork(wifiConfiguration);
    }
    private String getConnectedSSID() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        Log.d(TAG, "connected to " + ssid);
        return ssid;
    }
    //connects handset to specified wifi
    private void connectToWifi(String networkSSID, String networkPassword, WifiManager wifi) {
        if (!wifi.isWifiEnabled()) {
            Log.d(TAG, "wifi not turned on, turning on now");
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.enableNetwork(addWifiToSavedWifiConfiguration(networkSSID, networkPassword), true);
        Log.d(TAG, "connecting to " + networkSSID);
        //lock connection for non-internet i think?
        wifi.createWifiLock("WifiConnection");
    }//end connectToWifi

    //goes to webview activity at the specified URL sent in the INTENT_EXTRA
    private void openURL(String url) {
        Intent intent = new Intent(this, Webview.class);
        intent.putExtra(INTENT_EXTRA, url);
        startActivity(intent);
    }//end setURL

    private void resetTextBox(EditText textBox) {
        textBox.setText("");
    }
    private JSONObject resetParameters() {
        return new JSONObject();
    }


    private void addParameterToJSON(String inputKey, String inputValue, JSONObject json) throws JSONException {
        String key = inputKey;
        String value = inputValue;
        if (!key.isEmpty()) {
            json.put(key, value);
        }
        parametersTextView.setText(json.toString());
    }

    private boolean checkIfWifiIsConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else {
            return false;
        }
    }

    ;

//how sloppy are you tryna be here bruh?
    private void checkPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                },
                1);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                1);
    }

    public ArrayList<InetAddress> scanNetworkForIPAddresses(String deviceIP) {
        ArrayList<InetAddress> ret = new ArrayList<InetAddress>();
        int IPAddress = 0;
        String[] myIPArray = deviceIP.split("\\.");
        InetAddress currentAddressToPing;
        for (int i = 0; i <= 255; i++) {
            try {
                // build the next IP address
                currentAddressToPing = InetAddress.getByName(myIPArray[0] + "." +
                        myIPArray[1] + "." +
                        myIPArray[2] + "." +
                        IPAddress
                );
                // 50ms Timeout for the "ping"
                if (currentAddressToPing.isReachable(50)) {
                    ret.add(currentAddressToPing);
                }
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            IPAddress++;
        }
        return ret;
    }

    private class scanForNetworkDevices extends AsyncTask<String, Integer, Long> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectButton.setText("Scanning");
            connectButton.setBackgroundColor(Color.BLUE);
            connectButton.setTextColor(Color.WHITE);
            responseTextView.setText("Scanning the Network for Devices...");
        }

        protected Long doInBackground(String... addresses) {
            textContainer = " ------ Devices Found On Network ------- " + System.lineSeparator();
            Log.d(TAG, "Scanning for devices....");
            isScanningNetwork = true;
            ArrayList<InetAddress> inetAddresses = scanNetworkForIPAddresses("10.42.0.185");
            for (InetAddress address: inetAddresses) {
                textContainer += address.getCanonicalHostName() + System.lineSeparator();
                Log.d(TAG, address.getHostAddress());            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
            isScanningNetwork = false;
            Log.d(TAG, "Scanning Complete");
            responseTextView.setText(textContainer);
            connectButton.setText("Complete");
            connectButton.setBackgroundColor(Color.GREEN);
            connectButton.setTextColor(Color.BLACK);

        }
    }


    private void scanForDevice() {
        new scanForNetworkDevices().execute();
    };

    private class scanForAvailableNetworks extends AsyncTask<String, Integer, Long> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectButton.setText("Scanning");
            connectButton.setBackgroundColor(Color.BLUE);
            connectButton.setTextColor(Color.WHITE);
            responseTextView.setText("Scanning for Networks...");
        }

        protected Long doInBackground(String... addresses) {
            textContainer = " ------ Networks Found ------- " + System.lineSeparator();
            Log.d(TAG, "Scanning for networks....");
            isScanningNetwork = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            if(!checkIfWifiIsConnected()){
                wifiManager.setWifiEnabled(true);
            }
            wifiManager.startScan();
            List<ScanResult> results = wifiManager.getScanResults();
            for (ScanResult scans : results) {
                if (!scans.SSID.isEmpty()) {
                    Log.d(TAG, "Found " + scans.SSID + System.lineSeparator());
                    textContainer += "Found " + scans.SSID + System.lineSeparator();
                    if (scans.SSID.equals(SSID)) {
                        Log.d(TAG, scans.SSID + " matches" + System.lineSeparator());
                        textContainer += scans.SSID + "matches" + System.lineSeparator();
                        connectToWifi(SSID,PSK, wifiManager);
                        wifiManager.reconnect();
                    }
                }
            }
            return null;
        }


        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            isScanningNetwork = false;
            Log.d(TAG, "Scanning Complete");
            responseTextView.setText(textContainer);
            connectButton.setText("Complete");
            connectButton.setBackgroundColor(Color.GREEN);
            connectButton.setTextColor(Color.BLACK);
            if(getConnectedSSID().equals("\"" + SSID + "\"")){
                connectButton.setText("Connected");
            }

        }
    }
    private void scanForNetworks() {
        new scanForAvailableNetworks().execute();

    };





    private void handleSubmitButton() {
        //isEmpty check to prevent sending blank data. Used on key only so reset value is possible
        if(postCheckedTextView.isChecked()){
            boolean sucessfullySent = true;
            try {
                server_url = urlTextBox.getText().toString();
                url_extra = urlExtraTextBox.getText().toString();
                am.sendPostRequest(MainActivity.this, server_url + url_extra,parameterAsJSON, responseTextView);
            }catch (JSONException e) {
                e.printStackTrace();
                sucessfullySent = false;

            }
            if ((sucessfullySent)) {
                parameterAsJSON = resetParameters();
            } else {
                Log.i(TAG, "FAILED TO SEND, KEEPING PARAMETERS");
            }
        }
        if(getCheckBox.isChecked()){
               server_url = urlTextBox.getText().toString();
               am.sendGetRequest(MainActivity.this, server_url + url_extra, responseTextView);
            }
    };//end handleSubmit

    private void handleConnectButton()  {
//check if MatsyaAP is available - if yes ? (connect) : no (findDeviceONNetwork() ? setConnected() : setNotFound())
//        scanForDevice();
        scanForNetworks();
    }    //end handleConnectButton
private void handleRebootButtonClick(){
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
        server_url = urlTextBox.getText().toString();
        am.sendPostRequest(MainActivity.this, server_url+"wifi/connect",data, responseTextView);
    } catch (JSONException e) {
        e.printStackTrace();
    }
}
private void handleAddParameterButtonClick(){
    try {
        addParameterToJSON(keyTextBox.getText().toString(),valueTextBox.getText().toString(),parameterAsJSON);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    resetTextBox(keyTextBox);
    resetTextBox(valueTextBox);
}
private void handleResetParameterButtonClick(){
    parameterAsJSON = resetParameters();
    parametersTextView.setText("");

}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        parameterAsJSON = resetParameters();

        setContentView(R.layout.activity_main);
        //UI element assignments
        //TextView Elements
        responseTextView = findViewById(R.id.responseTextView);
        responseTextView.setMovementMethod(new ScrollingMovementMethod());
        //checkedTextView
        postCheckedTextView = findViewById(R.id.postCheckedTextView);
        parametersTextView = findViewById(R.id.parametersTextView);
        getCheckBox = findViewById(R.id.getCheckBox);

            //user input text boxes
        keyTextBox = findViewById(R.id.keyTextBox);
        valueTextBox = findViewById(R.id.valueTextBox);
        urlTextBox  = findViewById(R.id.urlTextBox);
        urlExtraTextBox  = findViewById(R.id.urlExtraTextBox);
            //buttons
        addParameterButton = findViewById(R.id.addParameterButton);
        addParameterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            handleAddParameterButtonClick();
            }
        });
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    handleSubmitButton();
                }
        });//end onclickListener
        resetParametersButton = findViewById(R.id.resetParametersButton);
        resetParametersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetParameterButtonClick();
            }
        });
       connectButton = findViewById(R.id.connectButton);
       connectButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               handleConnectButton();
           }
       });
        rebootButton = findViewById(R.id.rebootButton);
        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRebootButtonClick();
            }
        });
checkPermissions();
    }//end onCreate
}//endClass