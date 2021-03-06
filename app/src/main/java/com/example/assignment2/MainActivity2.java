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
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {

    private WifiManager wifiManager;
    private AssetManager assetManager;
    private static Context context;
    Timer timer;
    ProgressBar progressBar;
    TextView textCell1, textCell2, textCell3, textCell4, textCell5, textCell6, textCell7, textCell8, textCell9, textCell10, textCell11, textCell12, textCell13, textCell14, textCell15, textStatus, textScan, textThreshold, textTime;
    LinearLayout linearBorder;
    Button buttonStart, buttonStop2, buttonTrain2, buttonLocalize;
    Boolean suc;
    Integer count, location, t, threshold2, threshold3, numMac, numCells, wrongMacCount;
    Long startTime, endTime;
    Double threshold1;
    String[] macAddresses, mac;
    ArrayList<Integer> locationMemory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Integers
        count = 0;
        //TODO Update this after changing mac file
        numMac = 87;
        numCells = 15;
        threshold1 = 0.90;  // Max prob threshold
        threshold2 = 5;     // Max new scan iteration count
        threshold3 = 50;    // Max amount highest prob for specific cell
        startTime = 0L;
        wrongMacCount = 0;

        // Booleans
        suc = true;

        // Strings
        mac = readMacFile(numMac, getApplicationContext());

        // Lists
        locationMemory = new ArrayList<>(Collections.nCopies(numCells, 0));

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
        textTime = (TextView) findViewById(R.id.textTIME);

        // Create Linear Layout
        linearBorder = (LinearLayout) findViewById(R.id.linearBORDER);

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
        getMacAddresses(); // Load the mac addresses

        // Checking whether you got new scan results
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


        // Stop de process
        buttonStop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopIteration();
                textStatus.setText("stopped");
                borderChange(false);
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
                startTime = System.currentTimeMillis();

                if (suc) {
                    progressBar.setVisibility(View.VISIBLE);
                    textScan.setText("new");
                    textScan.setTextColor(Color.GREEN);
                    localizeMe();
                } else {
                    textScan.setText("old");
                    textScan.setTextColor(Color.RED);
                }
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
                        if (suc) {
                            textScan.setText("new");
                            textScan.setTextColor(Color.GREEN);
                            scan();
                        } else {
                            textScan.setText("old");
                            textScan.setTextColor(Color.RED);
                        }
                    }
                });
            }
        }, 10, 1000); //delay ??? This is the delay in milliseconds before task is to be executed.
                                  //period ??? This is the time in milliseconds between successive task executions.
    }

    public void stopIteration() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            progressBar.setVisibility(View.INVISIBLE);
        }
        endTime = System.currentTimeMillis();
        textTime.setText((endTime-startTime)+"ms");
        startTime = System.currentTimeMillis();
        locationMemory = new ArrayList<>(Collections.nCopies(numCells, 0));
    }

    public void getMacAddresses() {
        try {
            macAddresses = assetManager.list("pmf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setColors(Integer location) {
        switch (location) {
            case 1:
                textCell1.setBackgroundResource(R.color.colorAccent);
                textCell1.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 2:
                textCell2.setBackgroundResource(R.color.colorAccent);
                textCell2.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 3:
                textCell3.setBackgroundResource(R.color.colorAccent);
                textCell3.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 4:
                textCell4.setBackgroundResource(R.color.colorAccent);
                textCell4.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 5:
                textCell5.setBackgroundResource(R.color.colorAccent);
                textCell5.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 6:
                textCell6.setBackgroundResource(R.color.colorAccent);
                textCell6.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 7:
                textCell7.setBackgroundResource(R.color.colorAccent);
                textCell7.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 8:
                textCell8.setBackgroundResource(R.color.colorAccent);
                textCell8.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 9:
                textCell9.setBackgroundResource(R.color.colorAccent);
                textCell9.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 10:
                textCell10.setBackgroundResource(R.color.colorAccent);
                textCell10.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 11:
                textCell11.setBackgroundResource(R.color.colorAccent);
                textCell11.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 12:
                textCell12.setBackgroundResource(R.color.colorAccent);
                textCell12.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 13:
                textCell13.setBackgroundResource(R.color.colorAccent);
                textCell13.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 14:
                textCell14.setBackgroundResource(R.color.colorAccent);
                textCell14.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 15:
                textCell15.setBackgroundResource(R.color.colorAccent);
                textCell15.setTextColor(Color.parseColor("#FFFFFF"));
                break;
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
        textTime.setText("...");
    }


    public void scan() {
        float[] prob;
        prob = new float[numCells];
        float max_prob = 0;
        location = 0;
        t = 0;

        for (int l = 0; l < numCells; l++) {
            prob[l] = (float) 1 / numCells;
        }

        wrongMacCount = 0;
        while (max_prob <= threshold1 && t <= threshold2) {

            wifiManager.startScan();
            List<ScanResult> scanResults = wifiManager.getScanResults();
            count = 0;

            for (ScanResult scanResult : scanResults) {
                int j = 0;
                for (String macAddress : macAddresses) { //TODO save max prop at location in this loop
                    if (scanResult.BSSID.equals(mac[j])) {
                        count += 1;
                        String file = "pmf/" + macAddress;
                        float[][] mac_temp = readPMFFiles(numCells, file, getApplicationContext());
                        int level = Math.abs(scanResult.level);
                        float sum_prob = 0;

                        for (int k = 0; k < numCells; k++) {
                            prob[k] = prob[k] * mac_temp[k][level];
                            sum_prob += prob[k];
                        }

                        for (int g = 0; g < numCells; g++) {
                            prob[g] /= sum_prob;
                            if (prob[g] > max_prob) {
                                max_prob = prob[g];
                                location = g + 1;
                            }
                        }

                        // Keep track of highest prob cells
                        int index = location-1;
                        int value = locationMemory.get(index); // TODO List with location count highest prob
                        value += 1;
                        locationMemory.set(index, value);

                    } else {
                        wrongMacCount += 1;
                    }
                    j++;
                }
            }
            t++;
        }
        textThreshold.setText(String.valueOf(max_prob));
        System.out.println("Done with while loop");

        if (t == (threshold2 + 1) && location != 15 && numMac/2 > wrongMacCount) {
            textStatus.setText("waiting...");
            for (int i = 0; i<numCells; i++) {
                if (locationMemory.get(i) > threshold3) {
                    textStatus.setText("successful");
                    setColors(i+1);
                    stopIteration();
                    borderChange(true);
                }
            }
        } else if (numMac/2 <= wrongMacCount) {
            textStatus.setText("no ap found");
            stopIteration();
            borderChange(false);
        } else if (System.currentTimeMillis() >= (startTime+25000)) {
            textStatus.setText("timeout");
            stopIteration();
            borderChange(false);
        } else {
            textStatus.setText("successful");
            setColors(location);
            stopIteration();
            borderChange(true);
        }
    }

    public static String[] readMacFile(int numTypes, Context context) {
        MainActivity2.context = context;
        String[] macTable = new String[numTypes];
        try{
            InputStreamReader fileReader = new InputStreamReader(MainActivity2.context.getAssets().open("mac_addresses.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
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
            String line;
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

    public void borderChange(Boolean success) {
        if (success) {
            linearBorder.setBackgroundResource(R.drawable.border2);
        } else {
            linearBorder.setBackgroundResource(R.drawable.border3);
        }

        new CountDownTimer(1000, 10) {
            @Override
            public void onTick(long arg0) {
                // Do nothing
            }
            @Override
            public void onFinish() {
                linearBorder.setBackgroundResource(R.drawable.border);
            }
        }.start();
    }

}
