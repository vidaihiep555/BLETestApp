package com.enclave.blesensor.entity;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanRecord;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.SparseArray;

import java.util.List;

/**
 * Created by root on 4/15/16.
 */
public class TemperatureBeacon extends Beacon {
    /* Full Bluetooth UUID that defines the Health Thermometer Service */
    public static final ParcelUuid THERM_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");
    /* Short-form UUID that defines the Health Thermometer service */
    private static final int UUID_SERVICE_THERMOMETER = 0x1809;

    private String mName;
    private float mCurrentTemp;
    private float mCurrentHumidity;
    private float mCurrentCarbon;
    //Device metadata
    private int mSignal;
    private String mAddress;

    /* Builder for Lollipop+ */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TemperatureBeacon(ScanRecord record, String deviceAddress, int rssi) {
        mSignal = rssi;
        mAddress = deviceAddress;

        mName = record.getDeviceName();

        byte[] datax = record.getServiceData(THERM_SERVICE);
        SparseArray<byte[]> data2 = record.getManufacturerSpecificData();
        byte[] data = data2.get(data2.keyAt(0));
        if (data.length >= 4) {
            mCurrentTemp = bytesToFloat(data[0], data[1], data[2], data[3]);
        }
        if (data.length >= 8) {
            mCurrentHumidity = bytesToFloat(data[4], data[5], data[6], data[7]);
        }
        if (data.length >= 12) {
            mCurrentTemp = bytesToFloat(data[8], data[9], data[10], data[11]);
        }

        /*if (datax != null) {
            mCurrentTemp = parseTemp(data);
        } else {
            mCurrentTemp = 0f;
        }*/
    }

    /* Builder for pre-Lollipop */
    public TemperatureBeacon(List<AdRecord> records, String deviceAddress, int rssi) {
        mSignal = rssi;
        mAddress = deviceAddress;

        for (AdRecord packet : records) {
            //Find the device name record
            if (packet.getType() == AdRecord.TYPE_NAME) {
                mName = AdRecord.getName(packet);
            }
            //Find the service data record that contains our service's UUID
            //&& AdRecord.getServiceDataUuid(packet) == UUID_SERVICE_THERMOMETER
            if (packet.getType() == AdRecord.TYPE_MANUFACTURER_SPECIFIC_DATA) {
                byte[] data = AdRecord.getServiceData(packet);
                //mCurrentTemp = parseTemp(data);

                if (data.length >= 4) {
                    mCurrentTemp = bytesToFloat(data[0], data[1], data[2], data[3]);
                }
                if (data.length >= 8) {
                    mCurrentHumidity = bytesToFloat(data[4], data[5], data[6], data[7]);
                }
                if (data.length >= 12) {
                    mCurrentTemp = bytesToFloat(data[8], data[9], data[10], data[11]);
                }
            }
        }
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    private static float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8) + (unsignedByteToInt(b2) << 16), 24);
        return (float) (mantissa * Math.pow(10, b3));
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded signed value.
     */
    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    private float parseTemp(byte[] serviceData) {
        /*
         * Temperature data is two bytes, and precision is 0.5degC.
         * LSB contains temperature whole number
         * MSB contains a bit flag noting if fractional part exists
         */
        float temp = (serviceData[0] & 0xFF);
        if ((serviceData[1] & 0x80) != 0) {
            temp += 0.5f;
        }

        return temp;
    }

    public String getName() {
        return mName;
    }

    public int getSignal() {
        return mSignal;
    }

    public float getCurrentTemp() {
        return mCurrentTemp;
    }

    public float getmCurrentHumidity() {
        return mCurrentHumidity;
    }

    public float getmCurrentCarbon() {
        return mCurrentCarbon;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmCurrentTemp(float mCurrentTemp) {
        this.mCurrentTemp = mCurrentTemp;
    }

    public void setmCurrentHumidity(float mCurrentHumidity) {
        this.mCurrentHumidity = mCurrentHumidity;
    }

    public void setmCurrentCarbon(float mCurrentCarbon) {
        this.mCurrentCarbon = mCurrentCarbon;
    }

    public void setmSignal(int mSignal) {
        this.mSignal = mSignal;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public TemperatureBeacon(){

    }

    @Override
    public String toString() {
        return String.format("%s (%ddBm): %.1fC", mName, mSignal, mCurrentTemp);
    }
}
