package com.enclave.blesensor.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.enclave.blesensor.R;
import com.enclave.blesensor.entity.TemperatureBeacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Albert on 21/04/2016.
 */
public class BeaconAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<TemperatureBeacon> listDataHeader;
    private HashMap<String, List<TemperatureBeacon>> listDataChild;

    public BeaconAdapter(Context context, List<TemperatureBeacon> listDataHeader, HashMap<String, List<TemperatureBeacon>> listDataChild) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listDataChild;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final TemperatureBeacon beacon = (TemperatureBeacon) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.listview_item, null);
        }

        TextView txtTemperature = (TextView) convertView
                .findViewById(R.id.tvTemperature);
        //Set color based on temperature
        final int textColor = getTemperatureColor(beacon.getCurrentTemp());
        txtTemperature.setText(String.format("%.1f\u00B0C", beacon.getCurrentTemp()));
        txtTemperature.setTextColor(textColor);

        TextView txtHumidity = (TextView) convertView
                .findViewById(R.id.tvHumidity);
        txtHumidity.setText(String.valueOf((int) beacon.getmCurrentHumidity()));
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TemperatureBeacon beacon = (TemperatureBeacon) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.listview_group, null);
        }

        TextView txtAddress = (TextView) convertView
                .findViewById(R.id.tvAddress);
        txtAddress.setTypeface(null, Typeface.BOLD);
        txtAddress.setText(beacon.getAddress());

        TextView txtSignal = (TextView) convertView
                .findViewById(R.id.tvSignal);
        txtSignal.setTypeface(null, Typeface.BOLD);
        txtSignal.setText(String.format("%ddBm", beacon.getSignal()));

        return convertView;
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition).getAddress())
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition).getAddress()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void updateDataSet(HashMap<String, TemperatureBeacon> mBeacons) {
        this.listDataHeader.clear();
        for (Map.Entry e : mBeacons.entrySet()) {
            TemperatureBeacon beacon = (TemperatureBeacon) e.getValue();

            this.listDataHeader.add(beacon);
            List<TemperatureBeacon> tempList = new ArrayList<TemperatureBeacon>();
            tempList.add(beacon);
            this.listDataChild.put(beacon.getAddress(), tempList);
        }
        int y = 0;
    }

    private int getTemperatureColor(float temperature) {
        //Color range from 0 - 40 degC
        float clipped = Math.max(0f, Math.min(40f, temperature));

        float scaled = ((40f - clipped) / 40f) * 255f;
        int blue = Math.round(scaled);
        int red = 255 - blue;

        return Color.rgb(red, 0, blue);
    }
}
