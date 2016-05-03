package com.enclave.blesensor.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.enclave.blesensor.R;

/**
 * Created by Albert on 14/04/2016.
 */
public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnSave;
    private SeekBar seekBarScanTime, seekBarStopTime;
    private TextView txtvScan, txtvStop;
    public static final int OFF_SET_PERIOD = 1;
    public static final int DEFAULT_SCAN_PERIOD = 5;
    public static final int DEFAULT_STOP_PERIOD = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSaveSetting();
                finish();
            }
        });

        seekBarScanTime = (SeekBar) findViewById(R.id.skbScanTime);

        seekBarScanTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextviewScan(progress + OFF_SET_PERIOD);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarStopTime = (SeekBar) findViewById(R.id.skbStopTime);
        seekBarStopTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextviewStop(progress + OFF_SET_PERIOD);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtvScan = (TextView) findViewById(R.id.tvScanTime);
        txtvStop = (TextView) findViewById(R.id.txtvStopTime);

        loadSetting();
    }

    private void loadSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(R.string.ble_setting), Context.MODE_PRIVATE);
        if (sharedPreferences == null) {
            //Save as default values
            sharedPreferences = this.getSharedPreferences(this.getString(R.string.ble_setting), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(this.getString(R.string.scan_time_period), DEFAULT_SCAN_PERIOD);
            editor.putInt(this.getString(R.string.stop_time_period), DEFAULT_STOP_PERIOD);
            editor.apply();
            updateSettingView(sharedPreferences);
        } else {
            updateSettingView(sharedPreferences);
        }
    }

    public void updateSettingView(SharedPreferences sharedPreferences) {
        int scanPeriod = sharedPreferences.getInt(this.getString(R.string.scan_time_period), DEFAULT_SCAN_PERIOD);
        int stopPeriod = sharedPreferences.getInt(this.getString(R.string.stop_time_period), DEFAULT_STOP_PERIOD);
        seekBarScanTime.setProgress(scanPeriod - OFF_SET_PERIOD);
        seekBarStopTime.setProgress(stopPeriod - OFF_SET_PERIOD);
        updateTextviewScan(scanPeriod);
        updateTextviewStop(stopPeriod);
    }

    public void doSaveSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getString(R.string.ble_setting), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(this.getString(R.string.scan_time_period), this.seekBarScanTime.getProgress() + OFF_SET_PERIOD);
        editor.putInt(this.getString(R.string.stop_time_period), this.seekBarStopTime.getProgress() + OFF_SET_PERIOD);

        //Save
        editor.apply();
    }

    public void updateTextviewScan(int period) {
        this.txtvScan.setText(this.getString(R.string.tv_scan_time_label) + period + this.getString(R.string.tv_time_second_label));
    }

    public void updateTextviewStop(int period) {
        this.txtvStop.setText(this.getString(R.string.tv_stop_time_label) + period + this.getString(R.string.tv_time_second_label));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
