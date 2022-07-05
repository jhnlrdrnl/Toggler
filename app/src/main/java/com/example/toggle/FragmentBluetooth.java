package com.example.toggle;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;

public class FragmentBluetooth extends Fragment implements AdapterView.OnItemClickListener {

    private Activity bluetoothActivity;
    private static final int RESULT_OK = 1;
    private BluetoothAdapter bluetoothAdapter;
    private SwitchMaterial bluetoothSwitch;
    private Button scanButton;
    private ListView listView;
    private ArrayList arrayList;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothActivity.registerReceiver(broadcastReceiver, intentFilter);
        Log.d("Toggle", "Bluetooth fragment initiated");
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        bluetoothActivity = activity;
        Log.d("Toggle", "Bluetooth activity initiated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothActivity.unregisterReceiver(broadcastReceiver);
        Log.d("Toggle", "Stopping bluetooth activity");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View bluetoothView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        bluetoothSwitch = bluetoothView.findViewById(R.id.bluetoothSwitch);
        BluetoothManager bluetoothManager = (BluetoothManager) bluetoothActivity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        scanButton = (Button) bluetoothView.findViewById(R.id.scanButton);
        listView = (ListView) bluetoothView.findViewById(R.id.bluetoothList);
        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<String>(bluetoothActivity, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(FragmentBluetooth.this);

        checkBluetoothAvailability();

        bluetoothSwitch.setOnClickListener(view -> {
            if (bluetoothSwitch.isChecked()) {
                setBluetoothOn();
            } else {
                setBluetoothOff();
            }
        });

        scanButton.setOnClickListener(view -> {
            if (!bluetoothAdapter.isEnabled()) {
                postToast("BLUETOOTH_DEACTIVATED");
                Log.d("Toggle", "Bluetooth is already deactivated");
            } else {
                discoverNew();
            }
        });

        return bluetoothView;
    }

    public void checkBluetoothAvailability() {
        if (bluetoothAdapter == null) {
            bluetoothSwitch.setEnabled(false);
            scanButton.setEnabled(false);

            postToast("BLUETOOTH_UNAVAILABLE");
            Log.d("Toggle", "Bluetooth is not available on this device");
        }
    }

    public void setBluetoothOn() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(bluetoothActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(bluetoothIntent, 0);
            }
            IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            bluetoothActivity.registerReceiver(broadcastReceiver, btIntent);

            postToast("BLUETOOTH_ACTIVATED");
            Log.d("Toggle", "Bluetooth is already activated");
        }
    }

    public void setBluetoothOff() {
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(bluetoothActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.disable();
                IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                bluetoothActivity.registerReceiver(broadcastReceiver, btIntent);

                postToast("BLUETOOTH_DEACTIVATED");
                Log.d("Toggle", "Bluetooth is already deactivated");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            postToast("BLUETOOTH_ACTIVATED");
            Log.d("Toggle", "Bluetooth is already activated");
        }
    }

    public void discoverNew() {
        if (ActivityCompat.checkSelfPermission(bluetoothActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();

            postToast("BLUETOOTH_SCAN");
            Log.d("Toggle", "Scanning nearby Bluetooth Devices");
        }
    }


    public void postToast(String message) {
        switch (message) {
            case "BLUETOOTH_UNAVAILABLE":
                Toast.makeText(bluetoothActivity, "Bluetooth is not available on this device", Toast.LENGTH_SHORT).show();
            case "BLUETOOTH_ACTIVATED":
                Toast.makeText(bluetoothActivity, "Bluetooth is already activated", Toast.LENGTH_SHORT).show();
                break;
            case "BLUETOOTH_DEACTIVATED":
                Toast.makeText(bluetoothActivity, "Bluetooth is already deactivated", Toast.LENGTH_SHORT).show();
                break;
            case "BLUETOOTH_SCAN":
                Toast.makeText(bluetoothActivity, "Scanning nearby Bluetooth Devices", Toast.LENGTH_SHORT).show();
                break;
            case "BLUETOOTH_SCAN_FINISHED":
                Toast.makeText(bluetoothActivity, "Bluetooth scanning finished", Toast.LENGTH_SHORT).show();
                break;
            case "NO_BLUETOOTH_DEVICES_FOUND":
                Toast.makeText(bluetoothActivity, "No Bluetooth devices found nearby", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(bluetoothActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    arrayList.add(device.getName() + ": " + device.getAddress());
                }
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (ActivityCompat.checkSelfPermission(bluetoothActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();

            String deviceName = bluetoothDevices.get(i).getName();
            String deviceAddress = bluetoothDevices.get(i).getAddress();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // Log.d(TAG, "Trying to pair with " + deviceName +  " : " + deviceAddress);
                bluetoothDevices.get(i).createBond();
            }
        }
    }
}
