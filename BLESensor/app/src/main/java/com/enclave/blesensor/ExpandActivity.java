package com.enclave.blesensor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.enclave.blesensor.adapter.BeaconAdapter;
import com.enclave.blesensor.entity.TemperatureBeacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by Albert on 21/04/2016.
 */
public class ExpandActivity extends Activity {

    BeaconAdapter expListAdapter;
    ExpandableListView expListView;
    List<TemperatureBeacon> listDataHeader;
    HashMap<String, List<TemperatureBeacon>> listDataChild;
    HashMap<String, TemperatureBeacon> mBeacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_expand);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        expListAdapter = new BeaconAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(expListAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
//                 Toast.makeText(getApplicationContext(),
//                 "Group Clicked " + listDataHeader.get(groupPosition),
//                 Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
//        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v,
//                                        int groupPosition, int childPosition, long id) {
//                // TODO Auto-generated method stub
//                Toast.makeText(
//                        getApplicationContext(),
//                        listDataHeader.get(groupPosition)
//                                + " : "
//                                + listDataChild.get(
//                                listDataHeader.get(groupPosition)).get(
//                                childPosition), Toast.LENGTH_SHORT)
//                        .show();
//                return false;
//            }
//        });
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<TemperatureBeacon>();
        listDataChild = new HashMap<String, List<TemperatureBeacon>>();

        mBeacons = new HashMap<>();
        TemperatureBeacon beacon1 = new TemperatureBeacon();
        beacon1.setmAddress("LeDinhDuong");
        beacon1.setmName("ASDDAS");
        beacon1.setmSignal(70);
        beacon1.setmCurrentTemp(40);
        beacon1.setmCurrentHumidity(3062);
        beacon1.setmCurrentCarbon(22);

        TemperatureBeacon beacon2 = new TemperatureBeacon();
        beacon2.setmAddress("LeDinhDuong2");
        beacon2.setmName("ZZZZZZ");
        beacon2.setmSignal(70);
        beacon2.setmCurrentTemp(40);
        beacon2.setmCurrentHumidity(30);
        beacon2.setmCurrentCarbon(22);

        mBeacons.put(beacon1.getAddress(), beacon1);
        mBeacons.put(beacon2.getAddress(), beacon2);

        for (Entry e: mBeacons.entrySet()) {
            TemperatureBeacon beacon = (TemperatureBeacon) e.getValue();
            listDataHeader.add(beacon);
            List<TemperatureBeacon> tempList = new ArrayList<TemperatureBeacon>();
            tempList.add(beacon);
            listDataChild.put(beacon.getAddress(), tempList);
        }

        // Adding child data
        /*listDataHeader.add("Top 250");
        listDataHeader.add("Now Showing");

        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);*/
    }

}
