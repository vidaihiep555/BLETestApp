package com.enclave.blesensor.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.enclave.blesensor.R;
import com.enclave.blesensor.adapter.BeaconAdapter;
import com.enclave.blesensor.entity.TemperatureBeacon;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 29/04/2016.
 */
public class BLEReader extends AppCompatActivity {

    /**
     * Request code when request enable Bluetooth.
     */
    protected static final int ENABLE_BLUETOOTH_REQUEST = 1;

    /**
     * Request code when request location access.
     */
    protected static final int PERMISSION_COARSE_LOCATION_REQUEST = 2;

    /**
     * Request code when request enable location.
     */
    protected static final int ENABLE_LOCATION_REQUEST = 3;

    /**
     * Indicate bluetooth enabled or not.
     */
    protected boolean isBluetoothEnabled = false;

    /**
     * Indicate location enabled or not.
     */
    protected boolean isLocationEnabled = false;

    /**
     * Indicate runtime location permission granted or not.
     */
    protected boolean isLocationPermissionGranted = false;

    /**
     * Progress bar to indicate scan is running or not.
     */
    private ProgressBar progressBarScan;

    /**
     * Bluetooth adapter.
     */
    protected BluetoothAdapter mBluetoothAdapter;

    /**
     * List nearby Bluetooth devices.
     * Collect unique devices discovered, keyed by address.
     */
    private HashMap<String, TemperatureBeacon> mBeacons;

    /**
     * Beacon adapter.
     */
    private BeaconAdapter mBeaconAdapter;

    /**
     * List view display payload data of found devices.
     */
    private ExpandableListView expListView;

    /**
     * Scan and stop period get from setting.
     */
    private int scanPeriod, stopPeriod;


    List<TemperatureBeacon> listDataHeader;
    HashMap<String, List<TemperatureBeacon>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_activity);

        /*
         * Check for Bluetooth LE Support.
         * Finish the app if running device not support Bluetooth LE.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, this.getString(R.string.no_le_support), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        getViews();

        /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBeacons = new HashMap<>();


        isBluetoothEnabled = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isLocationEnabled = false;
            isLocationPermissionGranted = false;
        } else {
            isLocationEnabled = true;
            isLocationPermissionGranted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get scan and stop period
        getSettingProperties();

        // Check bluetooth
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter == null) {
                BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                mBluetoothAdapter = manager.getAdapter();
            }
            isBluetoothEnabled = false;
            requestEnableBluetooth();
        } else {
            isBluetoothEnabled = true;
            firstScan();
        }

        // Ask runtime location permission for android M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = false;
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION_REQUEST);
            } else {
                isLocationPermissionGranted = true;
                firstScan();
            }
            // Check location
            requestEnableLocation();
        } else {
            isLocationPermissionGranted = true;
            isLocationEnabled = true;
            firstScan();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST:
                if (resultCode == RESULT_OK) {
                    isBluetoothEnabled = true;
                    firstScan();
                } else {
                    finish();
                }
                break;
            case ENABLE_LOCATION_REQUEST:
                if (resultCode == RESULT_OK) {
                    isLocationEnabled = true;
                    firstScan();
                } else {
                    finish();
                }
                break;
            default:
                // unknown request
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int grantResult[]) {
        switch (requestCode) {
            case PERMISSION_COARSE_LOCATION_REQUEST:
                if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationPermissionGranted = true;
                    firstScan();
                } else {
                    finish();
                }
                break;
            default:
                //unknown request
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Cancel any scans in progress
        if (mHandler != null) {
            mHandler.removeCallbacks(mStopRunnable);
            mHandler.removeCallbacks(mStartRunnable);
        }
    }

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
                Intent intent = new Intent(BLEReader.this, SettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestEnableLocation() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_LOW_POWER));
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(final LocationSettingsResult settingsResult) {
                final Status status = settingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        isLocationEnabled = true;
                        firstScan();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        isLocationEnabled = false;
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    BLEReader.this,
                                    ENABLE_LOCATION_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        isLocationEnabled = false;
                        break;
                }
            }
        });
    }

    /**
     * This method is used to request to enable bluetooth.
     */
    private void requestEnableBluetooth() {
        progressBarVisibility(View.GONE);
        //Enable bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);
    }

    /**
     * First time call scan after resume()
     */
    private void firstScan() {
        if (isBluetoothEnabled && isLocationEnabled && isLocationPermissionGranted) {
            startScan();
        }
    }

    /**
     * Start scan in scanPeriod time second.
     */
    protected void startScan() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mStopRunnable);
            mHandler.postDelayed(mStopRunnable, scanPeriod * 1000);
        }
        progressBarVisibility(View.VISIBLE);
    }

    /**
     * This runnable is used to start scan.
     */
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    /**
     * This runnable is used to stop scan.
     */
    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    /**
     * Stop scan in scanPeriod time second.
     */
    protected void stopScan() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mStartRunnable);
            mHandler.postDelayed(mStartRunnable, stopPeriod * 1000);
        }
        if (mBeacons.isEmpty()) {
            Toast.makeText(getApplicationContext(), this.getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
        }
        progressBarVisibility(View.GONE);
    }

    /*
     * We have a Handler to process scan results on the main thread,
     * add them to our list adapter, and update the view
     */
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TemperatureBeacon beacon = (TemperatureBeacon) msg.obj;
            if (mBeacons != null) {
                mBeacons.put(beacon.getAddress(), beacon);
            }
            if (mBeaconAdapter != null) {
                mBeaconAdapter.updateDataSet(mBeacons);
                mBeaconAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * This method is used to get all views object from XML layout.
     */
    private void getViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
        }

        progressBarScan = (ProgressBar) findViewById(R.id.progressBarScan);
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        mBeaconAdapter = new BeaconAdapter(this, listDataHeader, listDataChild);

        expListView.setAdapter(mBeaconAdapter);

        if (expListView != null) {
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
        }

    }

    /**
     * This method is used to get settings properties.
     */
    private void getSettingProperties() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(R.string.ble_setting), Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            scanPeriod = sharedPreferences.getInt(this.getString(R.string.scan_time_period), SettingActivity.DEFAULT_SCAN_PERIOD);
            stopPeriod = sharedPreferences.getInt(this.getString(R.string.stop_time_period), SettingActivity.DEFAULT_STOP_PERIOD);
        } else {
            scanPeriod = SettingActivity.DEFAULT_SCAN_PERIOD;
            stopPeriod = SettingActivity.DEFAULT_STOP_PERIOD;
        }
    }

    /**
     * This method is used to show/hide the progressbar.
     * Show: visibility = View.VISIBLE
     * Hide: visibility = View.GONE
     */
    private void progressBarVisibility(final int visibility) {
        if (progressBarScan != null) {
            progressBarScan.setVisibility(visibility);
        }
    }

}
