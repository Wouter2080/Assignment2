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
    public static Context context;
    Timer timer;
    ProgressBar progressBar;
    TextView textCell1, textCell2, textCell3, textCell4, textCell5, textCell6, textCell7, textCell8, textCell9, textCell10, textCell11, textCell12, textCell13, textCell14, textCell15, textStatus, textScan, textThreshold;
    Button buttonStart, buttonStop2, buttonTrain2, buttonLocalize;
    Boolean suc;
    Integer count, location, t, threshold2, numMac, numCells;
    Double threshold1;
    AssetManager assetManager;
    String[] macAddresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Integers
        count = 0;
        numMac = 343;
        numCells = 15;
        threshold1 = 0.95;
        threshold2 = 5;

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
        textScan = (TextView) findViewById(R.id.textSCAN);
        textThreshold = (TextView) findViewById(R.id.textTHRESHOLD);

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
        getMacAddresses();

        // Checking whether you got new scan results
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    suc = true;
                    textScan.setTextColor(Color.GREEN);
                    textScan.setText("new");
                    System.out.println("Scan successful");
                } else {
                    suc = false;
                    textScan.setTextColor(Color.RED);
                    textScan.setText("old");
                    System.out.println("Old scan results");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);


        // Stop de process
        buttonStop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopIteration();
                textStatus.setText("stop");
            }
        });

        // Go to training activity
        buttonTrain2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Localize me only for one iteration then stop
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
                        String[] mac = readMacFile(numMac, getApplicationContext());
                        float[] prob;
                        prob = new float[numCells];
                        float max_prob = 0;
                        location = 0;
                        t = 0;

                        for (int l = 0; l < numCells; l++) {
                            prob[l] = (float) 1 / numCells;
                        }

                        while (max_prob <= threshold1 && t <= threshold2 ) {

                            wifiManager.startScan();
                            List<ScanResult> scanResults = wifiManager.getScanResults();
                            count = 0;

                            for (ScanResult scanResult : scanResults) {
                                int j = 0;
                                for (String macAddress : macAddresses) {
                                    if (scanResult.BSSID.equals(mac[j])) {
                                        count += 1;
                                        String file = "pmf/" + macAddress;
                                        float[][] mac_temp = readPMFFiles(numCells, file, getApplicationContext());
                                        int level = Math.abs(scanResult.level);
                                        float sum_freq = 0;

                                        for (int k = 0; k < numCells; k++) {
                                            prob[k] = prob[k] * mac_temp[k][level];
                                            sum_freq += prob[k];
                                        }

                                        for (int g = 0; g < numCells; g++) {
                                            prob[g] /= sum_freq;
                                            if (prob[g] > max_prob) {
                                                max_prob = prob[g];
                                                location = g + 1;
                                            }
                                        }
                                    } else {
                                        textStatus.setText("No match mac address" + scanResult.BSSID);
                                    }
                                    j++;
                                }
                            }
                            t++;
                            textThreshold.setText(String.valueOf(max_prob));
                        }
                        if (t == (threshold2 + 1) && location != 15) {
                            textStatus.setText("waiting...");
                            //textStatus.setText(String.valueOf(max_freq));
                        }
                        else {
                            setColors();
                            textStatus.setText("successful recognition");
                            stopIteration();
                        }
                    }
                });
            }
        }, 10, 1000); //delay − This is the delay in milliseconds before task is to be executed.
                                  //period − This is the time in milliseconds between successive task executions.
    }


    public static String[] readMacFile(int numTypes, Context context) {
        MainActivity2.context = context;
        String[] macTable = new String[numTypes];
        try{
            InputStreamReader fileReader = new InputStreamReader(MainActivity2.context.getAssets().open("mac_addresses.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            int i=0;
            while((line = bufferedReader.readLine())!=null){
                macTable[i] = line;
                i++;
            }
            bufferedReader.close();
        }catch(IOException ex){
            Log.w("error",ex.toString());
        }
        return macTable;
    }

    public static float[][] readPMFFiles(int cellNum, String fileName, Context context){
        MainActivity2.context = context;
        int rssSize = 256;
        float[][] rssTable = new float[cellNum][rssSize];
        try{
            InputStreamReader fileReader = new InputStreamReader(MainActivity2.context.getAssets().open(fileName));
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

    public void getMacAddresses() {
        try {
            macAddresses = assetManager.list("pmf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setColors() {
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

        textStatus.setText("...");
        textScan.setText("...");
        textScan.setTextColor(Color.parseColor("#696969"));
        textThreshold.setText("...");
    }


}
