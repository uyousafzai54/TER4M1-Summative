package com.max_belleville.messaging_app;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;


/**
 * show list of BLE devices
 */
public class DevicesFragment extends ListFragment {

    private final BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bleDiscoveryBroadcastReceiver;
    private IntentFilter bleDiscoveryIntentFilter;

    public DevicesFragment() {
        //Setup bluetooth adapter, start discovery.

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bleDiscoveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    //Upon finding a device call updateScan with that device

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                        getActivity().runOnUiThread(() -> updateScan(device));
                    }
                }
                //End scan, when discovery finished

                if(intent.getAction().equals((BluetoothAdapter.ACTION_DISCOVERY_FINISHED))) {
                    stopScan();
                }
            }
        };
        bleDiscoveryIntentFilter = new IntentFilter();
        bleDiscoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bleDiscoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Start bluetooth scan when main activity is opened.

        startScan();
    }

    @Override
    public void onResume() {
        //On open a minimized version restart discovery

        super.onResume();
        getActivity().registerReceiver(bleDiscoveryBroadcastReceiver, bleDiscoveryIntentFilter);
    }

    @Override
    public void onPause() {
        //On minimize app stop discovery.

        super.onPause();
        stopScan();
        getActivity().unregisterReceiver(bleDiscoveryBroadcastReceiver);
    }



    private void startScan() {
        //Ensure android version can read bluetooth LE.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           //If app doesn't have location perms needed display a message explaning why this app needs location perms.
            //Also request the perms

            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getText(R.string.location_permission_title));
                builder.setMessage(getText(R.string.location_permission_message));
                builder.setPositiveButton(android.R.string.ok,
                        (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0));
                builder.show();
                return;
            }
        }
        //Also start discovery

        bluetoothAdapter.startDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // ignore requestCode as there is only one in this fragment
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Handler(Looper.getMainLooper()).postDelayed(this::startScan,1); // run after onResume to avoid wrong empty-text
        } else {
            //If user rejects perms display

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getText(R.string.location_denied_title));
            builder.setMessage(getText(R.string.location_denied_message));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    private void updateScan(BluetoothDevice device) {
        //Ensure the device exists and starts with JDY (bluetooth module)

        if(device.getName()!=null) {
           if(device.getName().startsWith("JDY")) {
               //If so stop discovery and start the messaging fragment

               stopScan();
               Bundle args = new Bundle();
               args.putString("device", device.getAddress());
               Fragment fragment = new TerminalFragment();
               fragment.setArguments(args);
               getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
           }
        }
    }

    private void stopScan() {
        bluetoothAdapter.cancelDiscovery();
    }
}
