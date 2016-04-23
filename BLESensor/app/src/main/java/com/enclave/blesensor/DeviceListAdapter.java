package com.enclave.blesensor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Albert on 14/04/2016.
 */
public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<Device> deviceList = null;
    private LayoutInflater inflater = null;
    Context context = null;

    public DeviceListAdapter(Context context, ArrayList<Device> deviceList) {
        this.deviceList = deviceList;
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
    }

    private class ViewHolder {
        TextView tvTemperature;
        TextView tvHumidity;

        public ViewHolder(View item) {
            tvTemperature = (TextView) item.findViewById(R.id.tvTemperature);
            tvHumidity = (TextView) item.findViewById(R.id.tvHumidity);
        }

    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Device getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;

        if (view == null) {
            view = inflater.inflate(R.layout.list_row, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Device currentDevice = getItem(position);
        holder.tvTemperature.setText(currentDevice.getTemperature());
        holder.tvHumidity.setText(currentDevice.getHumidity());

        return view;
    }
}
