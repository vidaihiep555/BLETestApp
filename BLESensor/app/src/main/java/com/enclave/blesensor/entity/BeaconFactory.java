package com.enclave.blesensor.entity;

import java.util.List;

/**
 * Created by root on 4/19/16.
 */
public class BeaconFactory {
    public static Beacon getBeacon(String type, List<AdRecord> records, String deviceAddress, int rssi                                                                     ){
        if(type.equalsIgnoreCase("temperature")) {
            return null;
        }

        return null;
    }
}
