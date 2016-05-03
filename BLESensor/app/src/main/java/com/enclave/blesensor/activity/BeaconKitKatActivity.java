package com.enclave.blesensor.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.enclave.blesensor.entity.AdRecord;
import com.enclave.blesensor.R;
import com.enclave.blesensor.adapter.BeaconAdapter;
import com.enclave.blesensor.entity.TemperatureBeacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BeaconKitKatActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = BeaconKitKatActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private ProgressBar progressBarScan;
    private Toolbar toolbar;
    private BluetoothAdapter mBluetoothAdapter;
    /* Collect unique devices discovered, keyed by address */
    private HashMap<String, TemperatureBeacon> mBeacons;
    private BeaconAdapter mBeaconAdapter;
    private ExpandableListView expListView;
    List<TemperatureBeacon> listDataHeader;
    HashMap<String, List<TemperatureBeacon>> listDataChild;
    SharedPreferences sharedPreferences;
    private int scanPeriod, stopPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new  DialogInterface.OnDismissListener(){

                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        setContentView(R.layout.beacon_activity);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        progressBarScan = (ProgressBar) findViewById(R.id.progressBarScan);

        /*
         * We are going to display all the device beacons that we discover
         * in a list, using a custom adapter implementation
         */

        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        mBeaconAdapter = new BeaconAdapter(this, listDataHeader, listDataChild);

        expListView.setAdapter(mBeaconAdapter);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return false;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mBeacons = new HashMap<>();
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * side loads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, this.getString(R.string.no_le_support), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        this.sharedPreferences = this.getSharedPreferences(this.getString(R.string.ble_setting), Context.MODE_PRIVATE);
        if (sharedPreferences == null) {
            scanPeriod = SettingActivity.DEFAULT_SCAN_PERIOD;
            stopPeriod = SettingActivity.DEFAULT_STOP_PERIOD;
        } else {
            scanPeriod = sharedPreferences.getInt(this.getString(R.string.scan_time_period), SettingActivity.DEFAULT_SCAN_PERIOD);
            stopPeriod = sharedPreferences.getInt(this.getString(R.string.stop_time_period), SettingActivity.DEFAULT_STOP_PERIOD);
        }

        //Begin scanning for LE devices
        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        //Scan for devices advertising the thermometer service
        progressBarScan.setVisibility(View.VISIBLE);
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, scanPeriod * 1000);
    }

    private void stopScan() {
        if (mBeacons.isEmpty()) {
            Toast.makeText(getApplicationContext(), this.getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
        }
        progressBarScan.setVisibility(View.GONE);
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);

        mHandler.postDelayed(mStartRunnable, stopPeriod * 1000);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We need to parse out of the AD structures from the scan record
         */
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
        /*if (records.size() == 0) {
            Log.i(TAG, "Scan Record Empty");
        } else {
            Log.i(TAG, "Scan Record: "
                    + TextUtils.join(",", records));
        }*/

        /*
         * Create a new beacon from the list of obtains AD structures
         * and pass it up to the main thread
         */
        TemperatureBeacon beacon = new TemperatureBeacon(records, device.getAddress(), rssi);
        mHandler.sendMessage(Message.obtain(null, 0, beacon));
    }

    /*
     * We have a Handler to process scan results on the main thread,
     * add them to our list adapter, and update the view
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TemperatureBeacon beacon = (TemperatureBeacon) msg.obj;
            mBeacons.put(beacon.getAddress(), beacon);

            mBeaconAdapter.updateDataSet(mBeacons);
            mBeaconAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Intent intent = new Intent(BeaconKitKatActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

}
