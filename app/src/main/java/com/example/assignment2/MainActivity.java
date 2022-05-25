package com.example.assignment2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    Button buttonRssi, buttonTrain, buttonSave, buttonStop, buttonTest;

    Integer limit, count, rounds;
    Boolean start,suc;
    String data, dialogText;


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
        buttonTest = (Button) findViewById(R.id.buttonTEST);

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
                textScan.setText("...");
                count = 0;
                limit = 0;
                start = false;
            }
        });


        buttonSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });


        buttonTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
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
                textTrain.append(scanResult.BSSID + "," + scanResult.level + "," + scanResult.SSID);
                count++;
            } else {
                textTrain.append("," + scanResult.BSSID + "," + scanResult.level + "," + scanResult.SSID);
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


    private void writeToFile(String data, String filename){
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        root = new File(root, "RSSI-Data");
        if (!root.exists()) {root.mkdir();}
        root = new File(root , filename);
        try {
            FileOutputStream stream = new FileOutputStream(root);
            stream.write(data.getBytes());
            stream.close();
            Toast.makeText(MainActivity.this, "Saved as " + dialogText,Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            boolean bool = false;
            try {bool = root.createNewFile();} catch (IOException ex) {ex.printStackTrace();}
            if (bool){writeToFile(data,filename);} else {throw new IllegalStateException("Failed to create image file");}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("example.txt");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogText = input.getText().toString();
                if (dialogText.equals("")) {dialogText = "example.txt";}
                data = String.valueOf(textTrain.getText());
                writeToFile(data, dialogText);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

}