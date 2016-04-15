package com.enclave.blesensor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Albert on 14/04/2016.
 */
public class Settings extends Activity {

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSaveSetting();
                finish();
            }
        });

        loadSetting();
    }

    private void loadSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("bleSetting", Context.MODE_PRIVATE);
        if (sharedPreferences == null) {

        } else {

        }
    }

    public void doSaveSetting() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("bleSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Save
        editor.apply();
    }
}
