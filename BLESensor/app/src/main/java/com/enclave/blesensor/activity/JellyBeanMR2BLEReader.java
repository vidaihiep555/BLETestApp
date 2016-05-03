package com.enclave.blesensor.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.enclave.blesensor.entity.AdRecord;
import com.enclave.blesensor.entity.TemperatureBeacon;

import java.util.List;

public class JellyBeanMR2BLEReader extends BLEReader {

    private static final String TAG = JellyBeanMR2BLEReader.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onPause() {
        super.onPause();
        // Stop scan
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(scanCallback);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void startScan() {
        super.startScan();
        // Start scan
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.startLeScan(scanCallback);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void stopScan() {
        super.stopScan();
        // Stop scan
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(scanCallback);
        }
    }

    /**
     * Scan callback.
     */
    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
            List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
            TemperatureBeacon beacon = new TemperatureBeacon(records, device.getAddress(), rssi);
            if (mHandler != null) {
                mHandler.sendMessage(Message.obtain(null, 0, beacon));
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}