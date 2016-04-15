package com.enclave.blesensor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ListView lvDevice;
    private Context context = MainActivity.this;
    private ArrayList<Device> listDevices = new ArrayList<Device>();
    private Button btnNewActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_device);
        lvDevice = (ListView) findViewById(R.id.listView);
        addData();
        lvDevice.setAdapter(new DeviceListAdapter(context, listDevices));

        btnNewActivity = (Button) findViewById(R.id.btnActivity);
        btnNewActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });
    }

    private void addData() {
        for(int i = 0;i< 5; i++) {
            Device d = new Device();
            d.setHumidity("5" + i);
            d.setTemperature("2" + i);
            listDevices.add(d);
        }
    }
}
