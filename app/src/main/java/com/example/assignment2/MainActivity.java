package com.example.assignment2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

    TextView textRssi, textTrain, textCount, textScan;
    ScrollView scrollTrain, scrollRssi;
    Button buttonRssi, buttonTrain, buttonSave, buttonStop;

    Integer limit, count, rounds;
    Boolean start,suc;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Integers
        limit = 0;
        rounds = 100;

        // Booleans
        start = false;
        suc = true;

        // Create text views
        textRssi = (TextView) findViewById(R.id.textRSSI);
        textTrain = (TextView) findViewById(R.id.textTRAIN);
        textCount = (TextView) findViewById(R.id.textCOUNT);
        textScan = (TextView) findViewById(R.id.textSCAN);

        // Create scroll views
        scrollRssi = (ScrollView) findViewById(R.id.scrollRSSI);
        scrollTrain = (ScrollView) findViewById(R.id.scrollTRAIN);

        // Create buttons
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);
        buttonTrain = (Button) findViewById(R.id.buttonTRAIN);
        buttonSave = (Button) findViewById(R.id.buttonSAVE);
        buttonStop = (Button) findViewById(R.id.buttonSTOP);

        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);

                if (success) {
                    suc = true;
                    System.out.println("Scan successful");
                } else {
                    suc = false;
                    System.out.println("Old scan results");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);



        // Set listener for the button.
        buttonRssi.setOnClickListener(new OnClickListener() {
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
                scanResults.clear();
            }
        });

        // Set listener for the button.
        buttonTrain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textTrain.setText("");
                wifiManager.startScan();
                count = 0;
                limit = 0;
                start = true;

                if (suc) {
                    textScan.setText("new");
                    textScan.setTextColor(Color.GREEN);
                    startTimerThread();
                } else {
                    textScan.setText("old");
                    textScan.setTextColor(Color.RED);
                }
            }
        });

        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textTrain.setText("");
                textCount.setText("0");
                count = 0;
                limit = 0;
                start = false;
            }
        });


    }

    // onResume() registers the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
    }

    // onPause() unregisters the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
    }

    public void scan() {
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();

        for (ScanResult scanResult : scanResults) {
            if (count == 0) {
                textTrain.setText(textTrain.getText() + scanResult.BSSID + "," + scanResult.level + "," + scanResult.SSID);
                count++;
            } else {
                textTrain.setText(textTrain.getText() + "," + scanResult.BSSID + "," + scanResult.level + "," + scanResult.SSID);
                scrollTrain.fullScroll(View.FOCUS_DOWN);
            }
        }
        limit++;
        textTrain.append("\n");
        textCount.setText("" + limit);
        count = 0;
        scanResults.clear();
    }


    private void startTimerThread() {
        Thread th = new Thread(new Runnable() {
            public void run() {
                while (limit<rounds && start) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (suc) {
                                textScan.setText("new");
                                textScan.setTextColor(Color.GREEN);
                                scan();
                            } else {
                                textScan.setText("old");
                                textScan.setTextColor(Color.RED);
                                wifiManager.startScan();
                            }
                        }
                    });

                    try {
                        Thread.sleep(3030);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }

}