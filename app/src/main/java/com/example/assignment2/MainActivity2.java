package com.example.assignment2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {

    private WifiManager wifiManager;
    Timer timer;
    ProgressBar progressBar;
    TextView textCell1, textCell2, textCell3, textCell4, textCell5, textCell6, textCell7, textCell8, textCell9, textCell10, textCell11, textCell12, textCell13, textCell14, textCell15, textStatus;
    Button buttonStart, buttonStop2, buttonTrain2, buttonLocalize;
    Boolean suc;
    Integer count, location, t, threshold2, macAddresses, numCells;
    AssetManager assetManager;
    String[] files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Integers
        count = 0;
        // Booleans

        // Create text views
        textCell1 = (TextView) findViewById(R.id.textCELL1);
        textCell2 = (TextView) findViewById(R.id.textCELL2);
        textCell3 = (TextView) findViewById(R.id.textCELL3);
        textCell4 = (TextView) findViewById(R.id.textCELL4);
        textCell5 = (TextView) findViewById(R.id.textCELL5);
        textCell6 = (TextView) findViewById(R.id.textCELL6);
        textCell7 = (TextView) findViewById(R.id.textCELL7);
        textCell8 = (TextView) findViewById(R.id.textCELL8);
        textCell9 = (TextView) findViewById(R.id.textCELL9);
        textCell10 = (TextView) findViewById(R.id.textCELL10);
        textCell11 = (TextView) findViewById(R.id.textCELL11);
        textCell12 = (TextView) findViewById(R.id.textCELL12);
        textCell13 = (TextView) findViewById(R.id.textCELL13);
        textCell14 = (TextView) findViewById(R.id.textCELL14);
        textCell15 = (TextView) findViewById(R.id.textCELL15);
        textStatus = (TextView) findViewById(R.id.textSTATUS);

        // Create progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Create buttons
        buttonStart = (Button) findViewById(R.id.buttonSTART);
        buttonStop2 = (Button) findViewById(R.id.buttonSTOP2);
        buttonTrain2 = (Button) findViewById(R.id.buttonTRAIN2);
        buttonLocalize = (Button) findViewById(R.id.buttonLOCALIZE);

        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Asset manager
        assetManager = getAssets();
        fileNames();


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


        buttonStop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopIteration();
                textStatus.setText("stop");
            }
        });

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
                initializeColors();
                progressBar.setVisibility(View.VISIBLE);
                localizeMe();
            }
        });
    }


    private void localizeMe() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        macAddresses = 343;
                        numCells = 15;
                        String[] ap = apload(macAddresses, getApplicationContext());
                        float[] freq;
                        freq = new float[numCells];
                        for (int l = 0; l < numCells; l++) {
                            freq[l] = (float) 1 / numCells;
                        }
                        float max_freq = 0;
                        location = 0;
                        t = 0;
                        double threshold1 = 0.95;
                        threshold2 = 5;
                        while (max_freq <= threshold1 && t <= threshold2 ) {
                            wifiManager.startScan();
                            List<ScanResult> scanResult = wifiManager.getScanResults();
                            count = 0;
                            for (int i = 0; i < scanResult.size(); i++) {
                                int j = 0;
                                for (String value : files) {
                                    if (scanResult.get(i).BSSID.equals(ap[j])) {
                                        count += 1;
                                        String file = "pmf/" + value;
                                        float[][] ap_temp = fileload(numCells, file, getApplicationContext());
                                        int level = Math.abs(scanResult.get(i).level);
                                        float sum_freq = 0;
                                        for (int k = 0; k < numCells; k++) {
                                            freq[k] = freq[k] * ap_temp[k][level];
                                            sum_freq += freq[k];
                                        }
                                        for (int g = 0; g < numCells; g++) {
                                            freq[g] /= sum_freq;
                                            if (freq[g] > max_freq) {
                                                max_freq = freq[g];
                                                location = g + 1;
                                            }
                                            System.out.println(freq[g]);
                                            System.out.println(max_freq);
                                        }
                                    } else {
                                        textStatus.setText("No match mac address" + scanResult.get(i).BSSID);
                                    }
                                    j++;
                                }
                            }
                            t++;
                        }
                        if (t == (threshold2 + 1) && location != 15) {
                            textStatus.setText("waiting...");
                            //textStatus.setText(String.valueOf(max_freq));
                        }
                        else {
                            changeColors();
                            textStatus.setText("cell" + location);
                            stopIteration();
                        }
                    }
                });
            }
        }, 10, 1000); //delay − This is the delay in milliseconds before task is to be executed.
                                  //period − This is the time in milliseconds between successive task executions.
    }



    public static Context mCtx;
    public static String[] apload(int numTypes, Context context) {
        mCtx = context;
        String[] apTable = new String[numTypes];
        try{
            InputStreamReader fileReader = new InputStreamReader(mCtx.getAssets().open("mac_addresses.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            int i=0;
            while((line = bufferedReader.readLine())!=null){
                apTable[i] = line;
                i++;
            }
            bufferedReader.close();
        }catch(IOException ex){
            Log.w("error",ex.toString());
        }
        return apTable;
    }

    public static float[][] fileload(int cellNum, String fileName, Context context){
        mCtx = context;
        int rssSize = 256;
        float[][] rssTable = new float[cellNum][rssSize];
        try{
            InputStreamReader fileReader = new InputStreamReader(mCtx.getAssets().open(fileName));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            int cellIndex = 0;
            while((line = bufferedReader.readLine())!=null){
                String[] lineSplit = line.split(",");
                for(int i=0; i<rssSize; i++) {
                    rssTable[cellIndex][i] = Float.parseFloat(lineSplit[i]);
                }
                cellIndex++;
            }
            bufferedReader.close();
        }catch(IOException ex){
            System.out.println("error");
        }
        return rssTable;
    }


    public void stopIteration() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void fileNames() {
        try {
            files = assetManager.list("pmf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeColors() {
        switch (location) {
            case 1:
                textCell1.setBackgroundResource(R.color.colorAccent);
                textCell1.setTextColor(Color.parseColor("#FFFFFF"));
            case 2:
                textCell2.setBackgroundResource(R.color.colorAccent);
                textCell2.setTextColor(Color.parseColor("#FFFFFF"));
            case 3:
                textCell3.setBackgroundResource(R.color.colorAccent);
                textCell3.setTextColor(Color.parseColor("#FFFFFF"));
            case 4:
                textCell4.setBackgroundResource(R.color.colorAccent);
                textCell4.setTextColor(Color.parseColor("#FFFFFF"));
            case 5:
                textCell5.setBackgroundResource(R.color.colorAccent);
                textCell5.setTextColor(Color.parseColor("#FFFFFF"));
            case 6:
                textCell6.setBackgroundResource(R.color.colorAccent);
                textCell6.setTextColor(Color.parseColor("#FFFFFF"));
            case 7:
                textCell7.setBackgroundResource(R.color.colorAccent);
                textCell7.setTextColor(Color.parseColor("#FFFFFF"));
            case 8:
                textCell8.setBackgroundResource(R.color.colorAccent);
                textCell8.setTextColor(Color.parseColor("#FFFFFF"));
            case 9:
                textCell9.setBackgroundResource(R.color.colorAccent);
                textCell9.setTextColor(Color.parseColor("#FFFFFF"));
            case 10:
                textCell10.setBackgroundResource(R.color.colorAccent);
                textCell10.setTextColor(Color.parseColor("#FFFFFF"));
            case 11:
                textCell11.setBackgroundResource(R.color.colorAccent);
                textCell11.setTextColor(Color.parseColor("#FFFFFF"));
            case 12:
                textCell12.setBackgroundResource(R.color.colorAccent);
                textCell12.setTextColor(Color.parseColor("#FFFFFF"));
            case 13:
                textCell13.setBackgroundResource(R.color.colorAccent);
                textCell13.setTextColor(Color.parseColor("#FFFFFF"));
            case 14:
                textCell14.setBackgroundResource(R.color.colorAccent);
                textCell14.setTextColor(Color.parseColor("#FFFFFF"));
            case 15:
                textCell15.setBackgroundResource(R.color.colorAccent);
                textCell15.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }


    public void initializeColors() {
        int[] textCells = {R.id.textCELL1, R.id.textCELL2, R.id.textCELL3, R.id.textCELL4, R.id.textCELL5, R.id.textCELL6, R.id.textCELL7, R.id.textCELL8, R.id.textCELL9, R.id.textCELL10, R.id.textCELL11, R.id.textCELL12, R.id.textCELL13, R.id.textCELL14, R.id.textCELL15};
        for (Integer values : textCells) {
            TextView tc = (TextView) findViewById(values);
            tc.setBackgroundResource(R.color.colorCell);
            tc.setTextColor(Color.parseColor("#696969"));
        }
    }


}
