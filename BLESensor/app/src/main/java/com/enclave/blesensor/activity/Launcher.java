package com.enclave.blesensor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

public class Launcher extends Activity {

    private Toolbar toolbar;
    private Button btnScan, btnExit, btnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(this, LollipopBLEReader.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, JellyBeanMR2BLEReader.class);
            startActivity(intent);
        }
        finish();
    }

}
