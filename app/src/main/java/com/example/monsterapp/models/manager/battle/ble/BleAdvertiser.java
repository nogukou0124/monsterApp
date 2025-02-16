package com.example.monsterapp.models.manager.battle.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * BLEでAdvertiseを担当するクラス
 */
public class BleAdvertiser {
    /** Advertiser */
    @NonNull private final BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
    /** Subject */
    @NonNull private final PublishSubject<Boolean> advertiseSubject = PublishSubject.create();
    /** コンストラクタ */
    public BleAdvertiser() {};

    /**
     * アドバタイズを開始する
     * Subjectはアドバタイズ成功時にtrue,失敗時にfalseを返す
     * @return Subject
     */
    @SuppressLint("MissingPermission")
    public PublishSubject<Boolean> startAdvertising() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build();

        advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                advertiseSubject.onNext(true);
                advertiseSubject.onComplete();
            }

            @Override
            public void onStartFailure(int errorCode) {
                advertiseSubject.onNext(false);
                advertiseSubject.onComplete();
            }
        });

        return advertiseSubject;
    }
}
