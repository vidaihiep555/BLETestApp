package com.enclave.blesensor.entity;

import android.content.res.Resources;

import com.enclave.blesensor.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 4/15/16.
 */
public class AdRecord {
    /* An incomplete list of the Bluetooth GAP AD Type identifiers */
    public static final int TYPE_FLAGS = 0x1;
    public static final int TYPE_UUID16_INC = 0x2;
    public static final int TYPE_UUID16 = 0x3;
    public static final int TYPE_UUID32_INC = 0x4;
    public static final int TYPE_UUID32 = 0x5;
    public static final int TYPE_UUID128_INC = 0x6;
    public static final int TYPE_UUID128 = 0x7;
    public static final int TYPE_NAME_SHORT = 0x8;
    public static final int TYPE_NAME = 0x9;
    public static final int TYPE_TRANSMIT_POWER = 0xA;
    public static final int TYPE_CONN_INTERVAL = 0x12;
    public static final int TYPE_SERVICE_DATA = 0x16;
    public static final int TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFFFFFFFF;

    /*
     * Read out all the AD structures from the raw scan record
     */
    public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
        List<AdRecord> records = new ArrayList<AdRecord>();

        int index = 0;
        while (index < scanRecord.length) {
            int length = scanRecord[index++];
            //Done once we run out of records
            if (length == 0) break;

            int type = scanRecord[index];
            //Done if our record isn't a valid type
            if (type == 0) break;


            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

            records.add(new AdRecord(length, type, data));
            //Advance
            index += length;
        }

        return records;
    }

    /* Helper functions to parse out common data payloads from an AD structure */

    public static String getName(AdRecord nameRecord) {
        return new String(nameRecord.mData);
    }

    public static int getServiceDataUuid(AdRecord serviceData) {
        if (serviceData.mType != TYPE_MANUFACTURER_SPECIFIC_DATA) return -1;

        byte[] raw = serviceData.mData;
        //Find UUID data in byte array
        int uuid = (raw[1] & 0xFF) << 8;
        uuid += (raw[0] & 0xFF);

        return uuid;
    }

    public static byte[] getServiceData(AdRecord serviceData) {
        if (serviceData.mType != TYPE_MANUFACTURER_SPECIFIC_DATA) return null;

        byte[] raw = serviceData.mData;
        //Chop out the uuid
        return Arrays.copyOfRange(raw, 2, raw.length);
    }

    /* Model Object Definition */

    private int mLength;
    private int mType;
    private byte[] mData;

    public AdRecord(int length, int type, byte[] data) {
        mLength = length;
        mType = type;
        mData = data;
    }

    public int getLength() {
        return mLength;
    }

    public int getType() {
        return mType;
    }

    @Override
    public String toString() {
        switch (mType) {
            case TYPE_FLAGS:
                return Resources.getSystem().getString(R.string.type_flags);
            case TYPE_NAME_SHORT:
            case TYPE_NAME:
                return Resources.getSystem().getString(R.string.type_name);
            case TYPE_UUID16:
            case TYPE_UUID16_INC:
                return Resources.getSystem().getString(R.string.type_uuids16_inc);
            case TYPE_TRANSMIT_POWER:
                return Resources.getSystem().getString(R.string.type_transmit_power);
            case TYPE_CONN_INTERVAL:
                return Resources.getSystem().getString(R.string.type_connect_interval);
            case TYPE_SERVICE_DATA:
                return Resources.getSystem().getString(R.string.type_service_data);
            case TYPE_MANUFACTURER_SPECIFIC_DATA:
                return Resources.getSystem().getString(R.string.type_manufacturer_specific_data);
            default:
                return Resources.getSystem().getString(R.string.unknown_structure) + mType;
        }
    }
}
