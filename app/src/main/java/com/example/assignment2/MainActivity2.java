package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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
    Button buttonStart, buttonStop2, buttonTrain2;

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

        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);



        buttonTrain2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}