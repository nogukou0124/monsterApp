package com.example.monsterapp.utils;

import java.util.UUID;

/**
 * BLEで使用するUUIDを定義する
 */
public class BleConstants {
    /** BLE Service UUID */
    public static final UUID SERVICE_UUID = UUID.fromString("eb5ac374-b364-4b90-bf05-0000000000");
    /** BLE Characteristic UUID */
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("eb5ac374-b364-4b90-bf05-0000000001");
}
