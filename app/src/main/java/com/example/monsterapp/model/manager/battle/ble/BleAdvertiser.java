package com.example.monsterapp.model.manager.battle.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.util.ble.BleConstants;
import com.example.monsterapp.util.callback.BleCallback;

/**
 * BLEアドバタイザー（広告）を管理するクラス
 * 1. 自分の存在を周囲のデバイスに知らせる
 * 2. サービスUUIDを含めて広告することで、特定のアプリだけに発見される
 */
public class BleAdvertiser {
    // タグ（ログ出力用）
    private static final String TAG = "BleAdvertiser";

    // Bluetoothアダプター
    @Nullable private final BluetoothAdapter bluetoothAdapter;

    // BLEアドバタイザー
    @Nullable private BluetoothLeAdvertiser advertiser;

    // コールバック
    @Nullable private final BleCallback callback;

    // 広告中フラグ
    private boolean isAdvertising = false;

    /**
     * コンストラクタ
     * @param bluetoothAdapter Bluetoothアダプター
     * @param callback BLE通信のコールバック
     */
    public BleAdvertiser(@Nullable BluetoothAdapter bluetoothAdapter, @Nullable BleCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.callback = callback;

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetoothアダプターが利用できません");
            if (callback != null) {
                callback.onError("Bluetoothアダプターが利用できません");
            }
            return;
        }

        // アドバタイザーの取得
        advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        if (advertiser == null) {
            Log.e(TAG, "このデバイスはBLE広告をサポートしていません");
            if (callback != null) {
                callback.onError("このデバイスはBLE広告をサポートしていません");
            }
        }
    }

    /**
     * 広告を開始する
     * @return 広告開始に成功した場合はtrue
     */
    @SuppressLint("MissingPermission")
    public boolean startAdvertising() {
        if (advertiser == null || bluetoothAdapter == null) {
            Log.e(TAG, "アドバタイザーが初期化されていません");
            return false;
        }

        if (isAdvertising) {
            Log.d(TAG, "すでに広告中です");
            return true;
        }

        try {
            // デバイス名の設定
            bluetoothAdapter.setName(BleConstants.DEVICE_NAME);

            // 広告設定
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) // 低遅延モード
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)     // 高出力
                    .setConnectable(true)                                          // 接続可能
                    .setTimeout(0)                                                 // タイムアウトなし
                    .build();

            // 広告データ（サービスUUIDを含む）
            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)                                    // デバイス名を含める
                    .addServiceUuid(new ParcelUuid(BleConstants.SERVICE_UUID))     // サービスUUID
                    .build();

            // 応答データ（スキャン応答用）
            AdvertiseData scanResponse = new AdvertiseData.Builder()
                    .setIncludeTxPowerLevel(true)                                  // 送信電力を含める
                    .build();

            // 広告開始
            advertiser.startAdvertising(settings, data, scanResponse, advertiseCallback);

            Log.d(TAG, "広告を開始しました");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "広告の開始中にエラーが発生しました", e);
            if (callback != null) {
                callback.onError("広告の開始中にエラーが発生しました: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * 広告コールバック
     */
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "広告の開始に成功しました");
            isAdvertising = true;
        }

        @Override
        public void onStartFailure(int errorCode) {
            isAdvertising = false;
            String errorMessage = "広告の開始に失敗しました: ";

            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    errorMessage += "すでに広告が開始されています";
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    errorMessage += "広告データが大きすぎます";
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    errorMessage += "この機能はサポートされていません";
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    errorMessage += "内部エラーが発生しました";
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    errorMessage += "広告スロットが不足しています";
                    break;
                default:
                    errorMessage += "エラーコード: " + errorCode;
                    break;
            }

            Log.e(TAG, errorMessage);
            if (callback != null) {
                callback.onError(errorMessage);
            }
        }
    };

    /**
     * 広告を停止する
     */
    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        if (advertiser != null && isAdvertising) {
            try {
                advertiser.stopAdvertising(advertiseCallback);
                isAdvertising = false;
                Log.d(TAG, "広告を停止しました");
            } catch (Exception e) {
                Log.e(TAG, "広告の停止中にエラーが発生しました", e);
                if (callback != null) {
                    callback.onError("広告の停止中にエラーが発生しました: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 広告中かどうかを返す
     * @return 広告中の場合はtrue
     */
    public boolean isAdvertising() {
        return isAdvertising;
    }
}