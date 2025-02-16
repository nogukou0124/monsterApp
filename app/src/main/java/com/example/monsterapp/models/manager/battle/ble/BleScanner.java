package com.example.monsterapp.models.manager.battle.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import androidx.annotation.NonNull;

import java.util.Collections;

import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * BLEでスキャンを担当するクラス
 */
public class BleScanner {
    /** scanner */
    @NonNull private final BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    /** Subject */
    @NonNull private final PublishSubject<Boolean> scanSubject = PublishSubject.create();

    /** コンストラクタ */
    public BleScanner() {}

    /**
     * スキャンを開始する
     * subjectはスキャン成功時にtrue,失敗時にfalseを返す
     * @return subject
     */
    @SuppressLint("MissingPermission")
    public PublishSubject<Boolean> startScan() {
        ScanFilter filter = new ScanFilter.Builder().build();
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        scanner.startScan(Collections.singletonList(filter), settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                scanSubject.onNext(true);
                scanSubject.onComplete();
            }

            @Override
            public void onScanFailed(int errorCode) {
                scanSubject.onNext(false);
                scanSubject.onComplete();
            }
        });
        return scanSubject;
    }

}
