package com.example.assignment2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/**
 * Smart Phone Sensing Example 4. Wifi received signal strength.
 */
public class MainActivity extends Activity {

    /**
     * The wifi manager.
     */
    private WifiManager wifiManager;


    private TextView textRssi, textTrain, textCount;
    private Button buttonRssi, buttonTrain, buttonSave;
    private ScrollView scrollTrain, scrollRssi;

    private Integer limit, count;
    private Boolean start;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Integers
        limit = 0;

        // Booleans
        start = false;

        // Create text views
        textRssi = (TextView) findViewById(R.id.textRSSI);
        textTrain = (TextView) findViewById(R.id.textTRAIN);
        textCount = (TextView) findViewById(R.id.textCOUNT);

        // Create scroll views
        scrollRssi = (ScrollView) findViewById(R.id.scrollRSSI);
        scrollTrain = (ScrollView) findViewById(R.id.scrollTRAIN);

        // Create buttons
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);
        buttonTrain = (Button) findViewById(R.id.buttonTRAIN);
        buttonSave = (Button) findViewById(R.id.buttonSAVE);

        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Set listener for the button.
        buttonRssi.setOnClickListener(new OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                textRssi.setText("\n\tScan all access points:");
                wifiManager.startScan();
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult scanResult : scanResults) {
                    textRssi.setText(textRssi.getText() + "\n\tBSSID = "
                            + scanResult.BSSID + "    RSSI = "
                            + scanResult.level + "dBm");
                }
            }
        });

        // Set listener for the button.
        buttonTrain.setOnClickListener(new OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                textTrain.setText("");
                count = 0;
                limit = 0;
                start = true;
            }
        });


       Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("Dit is in thread");
                if (start) {
                    while (limit < 10) {
                        scan();
                        pause(2000);
                    }
                }
            }
        }; thread.start();
    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
    }

    public void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void scan() {
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();

        for (ScanResult scanResult : scanResults) {
            if (count == 0) {
                textTrain.setText(textTrain.getText() + scanResult.BSSID + "," + scanResult.level + "," + scanResult.SSID);
                count++;
                continue;
            }
            textTrain.setText(textTrain.getText() + "," + scanResult.BSSID + "," + scanResult.level + "," + scanResult.SSID);
            scrollTrain.fullScroll(View.FOCUS_DOWN);
        }
        limit++;
        textTrain.append("\n");
        textCount.setText("" + limit);
        count = 0;
    }


}