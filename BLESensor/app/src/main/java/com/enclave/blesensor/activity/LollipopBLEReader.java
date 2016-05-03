package com.enclave.blesensor.activity;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.enclave.blesensor.entity.TemperatureBeacon;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopBLEReader extends BLEReader {

    private static final String TAG = LollipopBLEReader.class.getSimpleName();

    /**
     * Bluetooth LE Scanner
     */
    public static BluetoothLeScanner mBluetoothLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop scan
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    @Override
    protected void startScan() {
        super.startScan();
        // Start scan
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.startScan(mScanCallback);
        }
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        // Stop scan
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    /**
     * Scan callback.
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        /**
         * This method is used to process the scan result.
         * @param result Scan result
         */
        private void processResult(ScanResult result) {
            Log.i(TAG, "New LE Device: " + result.getDevice().getName() + " @ " + result.getRssi());
            /*
             * Create a new beacon from the list of obtains AD structures
             * and pass it up to the main thread
             */
            TemperatureBeacon beacon = new TemperatureBeacon(result.getScanRecord(),
                    result.getDevice().getAddress(),
                    result.getRssi());
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
