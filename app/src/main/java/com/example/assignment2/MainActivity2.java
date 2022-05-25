package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    private WifiManager wifiManager;

    TextView textRssi, textTrain, textCount, textScan;
    ScrollView scrollTrain, scrollRssi;
    Button buttonStart, buttonStop2, buttonTrain2, buttonLocalize;

    Integer limit, count, rounds;
    Boolean start,suc;
    String data, dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Integers

        // Booleans

        // Create text views

        // Create scroll views

        // Create buttons
        buttonStart = (Button) findViewById(R.id.buttonSTART);
        buttonStop2 = (Button) findViewById(R.id.buttonSTOP2);
        buttonTrain2 = (Button) findViewById(R.id.buttonTRAIN2);
        buttonLocalize = (Button) findViewById(R.id.buttonLOCALIZE);

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



        buttonTrain2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });



        buttonLocalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private void oneIteration() {

    }

}