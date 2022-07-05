package com.example.toggle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;

public class FragmentWiFi extends Fragment {

    private Activity wifiActivity;
    public static final int ENABLE_REQUEST_WIFI = 1;
    private SwitchMaterial wifiSwitch;
    private WifiManager wifiManager;
    private ListView listView;
    private final ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View wifiView = inflater.inflate(R.layout.fragment_wifi, container, false);

        wifiSwitch = wifiView.findViewById(R.id.wifiSwitch);
        wifiManager = (WifiManager) wifiActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Button scanButton = (Button) wifiView.findViewById(R.id.scanButton);
        listView = wifiView.findViewById(R.id.wifiList);

        wifiSwitch.setOnClickListener(view -> {
            if (wifiSwitch.isChecked()) {
                setWifiOn();
            }
            else {
                setWifiOff();
            }
        });

        scanButton.setOnClickListener(view -> {
            if (!wifiManager.isWifiEnabled()) {
                postToast("WIFI_DEACTIVATED");
            } else {
                adapter = new ArrayAdapter<>(wifiActivity, android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);
                scanWifi();
            }
        });

        return wifiView;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        wifiActivity = activity;
    }

    private void setWifiOn() {
        // on API Level >= 29 wifiManager.setWifiEnabled is deprecated. Applications are not allowed to enable/disable Wi-Fi.
        // Check if API Level > 29
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            else {
                postToast("WIFI_ACTIVATED");
            }
        }
        else {
            // if API level >= 29, directly head over to Wi-Fi settings to manually set-up the Wi-Fi connection.
            if (!wifiManager.isWifiEnabled()) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                startActivityForResult(panelIntent, ENABLE_REQUEST_WIFI);
            }
            else {
                postToast("WIFI_ACTIVATED");
            }
        }
    }

    public void setWifiOff() {
        listView.setAdapter(null);
        // on API Level >= 29 wifiManager.setWifiEnabled is deprecated. Applications are not allowed to enable/disable Wi-Fi.
        // Check if API Level > 29
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            else {
                postToast("WIFI_DEACTIVATED");
            }
        }
        else {
            // if API level >= 29, directly head over to Wi-Fi settings to manually set-up the Wi-Fi connection.
            if (wifiManager.isWifiEnabled()) {
                Intent wifiIntent = new Intent(Settings.Panel.ACTION_WIFI);
                startActivityForResult(wifiIntent, ENABLE_REQUEST_WIFI);
            }
            else {
                postToast("WIFI_DEACTIVATED");
            }
        }
    }

    public void scanWifi() {
        arrayList.clear();
        wifiActivity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // on API level >= 28, wifiManager.startScan is deprecated. The ability for apps to trigger scan requests will be removed in a future release.
        wifiManager.startScan();
        postToast("WIFI_SCAN");
    }

    public void postToast(String message) {
        switch (message) {
            case "WIFI_ACTIVATED":
                Toast.makeText(wifiActivity, "Wi-Fi is already activated", Toast.LENGTH_SHORT).show();
                break;
            case "WIFI_DEACTIVATED":
                Toast.makeText(wifiActivity, "Wi-Fi is already deactivated", Toast.LENGTH_SHORT).show();
                break;
            case "WIFI_SCAN":
                Toast.makeText(wifiActivity, "Scanning nearby Wi-Fi Devices", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            wifiActivity.unregisterReceiver(this);

            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID);
                adapter.notifyDataSetChanged();
            }
        }
    };
}
