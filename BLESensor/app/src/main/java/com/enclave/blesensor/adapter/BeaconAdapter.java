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
    public View getChildView(int groupPosition,final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final TemperatureBeacon childText = (TemperatureBeacon) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild1 = (TextView) convertView
                .findViewById(R.id.tvTemperature);
        txtListChild1.setText(String.valueOf((int) childText.getCurrentTemp()));

        TextView txtListChild2 = (TextView) convertView
                .findViewById(R.id.tvTemperature);
        txtListChild2.setText(String.valueOf((int) childText.getmCurrentHumidity()));
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TemperatureBeacon headerTitle = (TemperatureBeacon) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.listview_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.tvAddress);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle.getAddress());

        TextView lblListHeader2 = (TextView) convertView
                .findViewById(R.id.tvHumi);
        lblListHeader2.setTypeface(null, Typeface.BOLD);
        lblListHeader2.setText(String.valueOf(headerTitle.getSignal()));

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

    public void setListDataHeader(List<TemperatureBeacon> listDataHeader) {
        this.listDataHeader = listDataHeader;
    }

    public void setListDataChild(HashMap<String, List<TemperatureBeacon>> listDataChild) {
        this.listDataChild = listDataChild;
    }

    public static void updateDataSet(HashMap<String, TemperatureBeacon> mBeacons) {
        for (Map.Entry e: mBeacons.entrySet()) {
            TemperatureBeacon beacon = (TemperatureBeacon) e.getValue();
            //this.l
            //this.listDataHeader.add(beacon);
            List<TemperatureBeacon> tempList = new ArrayList<TemperatureBeacon>();
            tempList.add(beacon);
            //listDataChild.put(beacon.getAddress(), tempList);
        }
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
