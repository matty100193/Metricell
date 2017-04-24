package com.example.matty.metricell;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,Runnable {

    TableLayout tableLayout;
    TextView loc,str,s_state;
    WifiManager wifiManager;
    List<ScanResult> wifi_list;

    String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loc=(TextView)findViewById(R.id.cell_loc);
        str=(TextView)findViewById(R.id.signal_str);
        s_state=(TextView)findViewById(R.id.service_state);
        checkPermissions();
        TelephonyManager telephonyManager=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener=new myPhoneStateListener();
        telephonyManager.listen(phoneStateListener,phoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        telephonyManager.listen(phoneStateListener,phoneStateListener.LISTEN_SERVICE_STATE);
        telephonyManager.listen(phoneStateListener,phoneStateListener.LISTEN_CELL_LOCATION);

        tableLayout=(TableLayout)findViewById(R.id.table_wifi);
        wifiManager=(WifiManager) getSystemService(WIFI_SERVICE);

        new Thread(this).start();
    }

    private void fillTable() {
        tableLayout.removeAllViews();
        for(int i=0;i<wifi_list.size();i++){
            TableRow tablerow=new TableRow(this);
            TextView text=new TextView(this);
            ScanResult current=wifi_list.get(i);
            text.setText(""+current.SSID);
            tablerow.addView(text);
            text=new TextView(this);
            text.setText("  "+current.level+"dbms");

            tablerow.addView(text);
            tableLayout.addView(tablerow);
        }
    }

    @Override
    public void run() {
        while(true){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                        wifi_list=wifiManager.getScanResults();
                        fillTable();

                }
            });
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class myPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            loc.setText("Cell Location: "+location);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            str.setText("Signal Strength : " + signalStrength.getGsmSignalStrength()+" dbms");
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            int value=serviceState.getState();
            String state="";
            if(value==0){
                state="STATE_IN_SERVICE";
            }
            else if(value==1){
                state="STATE_OUT_OF_SERVICE";
            }
            else if(value ==2){
                state="STATE_EMERGENCY_ONLY";
            }
            else{
                state="STATE_POWER_OFF";
            }
            s_state.setText("Service State: "+state);
        }
    }


    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }

}
