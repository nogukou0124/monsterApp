package com.example.monsterapp.util.ble;

import java.util.UUID;

/**
 * BLEで使用するUUIDを定義する
 */
public class BleConstants {
    /** デバイス名 */
    public static final String DEVICE_NAME = "MonsterDevice";

    /** BLE Service UUID */
    public static final UUID SERVICE_UUID =
            UUID.fromString("5f7bf097-c8b6-4246-9052-6d9910e43a61");

    /** BLE Characteristic UUID（対戦データ用） */
    public static final UUID BATTLE_CHARACTERISTIC_UUID =
            UUID.fromString("62961d5d-af44-4897-8c75-a897cc1dbf3e");

    /** BLE Characteristic UUID（接続確認用） */
    public static final UUID CONNECTION_CHARACTERISTIC_UUID =
            UUID.fromString("3db9d2e7-74de-4c90-a3d6-1f2df73de1e3");

    /** スキャンタイムアウト（ミリ秒） */
    public static final long SCAN_TIMEOUT = 10000;

    /** 接続タイムアウト（ミリ秒） */
    public static final long CONNECTION_TIMEOUT = 5000;
}