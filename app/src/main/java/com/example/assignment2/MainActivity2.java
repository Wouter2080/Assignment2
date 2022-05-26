package com.example.assignment2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
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
    AssetManager assetManager;
    String[] files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Integers

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
                        int macAddresses = 343, numCells = 15;
                        String[] ap = apload(macAddresses, getApplicationContext());
                        float[] freq;
                        freq = new float[numCells];
                        for (int l = 0; l < numCells; l++) {
                            freq[l] = (float) 1 / numCells;
                        }
                        float max_freq = 0;
                        int location = 0, t = 0;
                        double threshold1 = 0.95;
                        int threshold2 = 5;
                        while (max_freq <= threshold1 && t <= threshold2 ) {
                            wifiManager.startScan();
                            List<ScanResult> scanResult = wifiManager.getScanResults();
                            int count = 0;
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
                            //textStatus.setText("no match");
                            textStatus.setText(String.valueOf(max_freq));
                        }
                        else {
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
    public static String[] apload(int numTypes,Context context){
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
        int rssSize = 255;
        float[][] rssTable = new float[cellNum][rssSize];
        try{
            InputStreamReader fileReader = new InputStreamReader(mCtx.getAssets().open(fileName));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            int cellIndex = 0;
            while((line = bufferedReader.readLine())!=null){
                String[] lineSplit = line.split(",");
                for(int i=0; i<rssSize; i++)
                {
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

}
