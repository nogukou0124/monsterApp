package com.example.monsterapp.model.manager.battle.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.util.ble.BleConstants;
import com.example.monsterapp.util.callback.BleCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * BLEスキャナー機能を提供するクラス
 * 1. 周囲のデバイスをスキャンする
 * 2. 接続先を見つけたら自動的に接続する
 * 3. データを送受信する
 */
public class BleScanner {
    // タグ（ログ出力用）
    private static final String TAG = "BleScanner";

    // Bluetoothアダプター
    @Nullable private final BluetoothAdapter bluetoothAdapter;

    // BLEスキャナー
    @Nullable private BluetoothLeScanner scanner;

    // コールバック
    @Nullable private final BleCallback callback;

    // アプリケーションコンテキスト
    @NonNull private final Context context;

    // 接続先デバイス
    @Nullable private BluetoothGatt bluetoothGatt;

    // スキャン中フラグ
    private boolean isScanning = false;

    // 接続中フラグ
    private boolean isConnected = false;

    // バトルデータ用キャラクタリスティック
    @Nullable private BluetoothGattCharacteristic battleCharacteristic;

    // ハンドラー（タイムアウト処理用）
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * コンストラクタ
     * @param bluetoothAdapter Bluetoothアダプター
     * @param context アプリケーションコンテキスト
     * @param callback BLE通信のコールバック
     */
    public BleScanner(@Nullable BluetoothAdapter bluetoothAdapter,
                      @NonNull Context context,
                      @Nullable BleCallback callback) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.context = context;
        this.callback = callback;

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetoothアダプターが利用できません");
            if (callback != null) {
                callback.onError("Bluetoothアダプターが利用できません");
            }
            return;
        }

        // スキャナーの取得
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        if (scanner == null) {
            Log.e(TAG, "このデバイスはBLEスキャンをサポートしていません");
            if (callback != null) {
                callback.onError("このデバイスはBLEスキャンをサポートしていません");
            }
        }
    }

    /**
     * スキャンを開始する
     * @return スキャン開始に成功した場合はtrue
     */
    @SuppressLint("MissingPermission")
    public boolean startScan() {
        if (scanner == null) {
            Log.e(TAG, "スキャナーが初期化されていません");
            return false;
        }

        if (isScanning) {
            Log.d(TAG, "すでにスキャン中です");
            return true;
        }

        try {
            // フィルターの設定（特定のサービスUUIDを持つデバイスのみを検出）
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(BleConstants.SERVICE_UUID))
                    .build();
            filters.add(filter);

            // スキャン設定
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // 低遅延モード（バッテリー消費大）
                    .build();

            // スキャン開始
            scanner.startScan(filters, settings, scanCallback);
            isScanning = true;

            Log.d(TAG, "スキャンを開始しました");

            // タイムアウト設定
            handler.postDelayed(this::stopScan, BleConstants.SCAN_TIMEOUT);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "スキャンの開始中にエラーが発生しました", e);
            if (callback != null) {
                callback.onError("スキャンの開始中にエラーが発生しました: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * スキャンを停止する
     */
    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (scanner != null && isScanning) {
            try {
                scanner.stopScan(scanCallback);
                isScanning = false;
                Log.d(TAG, "スキャンを停止しました");
            } catch (Exception e) {
                Log.e(TAG, "スキャンの停止中にエラーが発生しました", e);
                if (callback != null) {
                    callback.onError("スキャンの停止中にエラーが発生しました: " + e.getMessage());
                }
            }
        }

        // タイムアウトハンドラーをキャンセル
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * スキャンコールバック
     */
    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();

            // デバイス名が一致するか確認
            if (deviceName != null && deviceName.equals(BleConstants.DEVICE_NAME)) {
                Log.d(TAG, "対象デバイスを発見しました: " + deviceName + ", " + device.getAddress());

                // スキャンを停止
                stopScan();

                // デバイスに接続
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            isScanning = false;
            String errorMessage = "スキャンに失敗しました: ";

            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    errorMessage += "すでにスキャンが開始されています";
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    errorMessage += "アプリケーション登録に失敗しました";
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    errorMessage += "この機能はサポートされていません";
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    errorMessage += "内部エラーが発生しました";
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
     * デバイスに接続する
     * @param device 接続先デバイス
     */
    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "デバイスに接続しています: " + device.getAddress());

        bluetoothGatt = device.connectGatt(context, false, gattCallback);

        // 接続タイムアウト設定
        handler.postDelayed(() -> {
            if (!isConnected && bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;

                Log.e(TAG, "接続がタイムアウトしました");
                if (callback != null) {
                    callback.onError("接続がタイムアウトしました");
                }
            }
        }, BleConstants.CONNECTION_TIMEOUT);
    }

    /**
     * GATTコールバック
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "デバイスに接続しました: " + gatt.getDevice().getAddress());
                    isConnected = true;

                    // タイムアウトハンドラーをキャンセル
                    handler.removeCallbacksAndMessages(null);

                    // サービス探索を開始
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "デバイスから切断されました");
                    isConnected = false;

                    if (callback != null) {
                        callback.onDisconnected();
                    }

                    close();
                }
            } else {
                Log.e(TAG, "接続状態の変更中にエラーが発生しました: " + status);
                isConnected = false;

                if (callback != null) {
                    callback.onError("接続状態の変更中にエラーが発生しました: " + status);
                }

                close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "サービスの探索に成功しました");

                // 対象のサービスを探す
                BluetoothGattService service = gatt.getService(BleConstants.SERVICE_UUID);

                if (service != null) {
                    Log.d(TAG, "対象のサービスを見つけました");

                    // 対象のキャラクタリスティックを探す
                    battleCharacteristic = service.getCharacteristic(
                            BleConstants.BATTLE_CHARACTERISTIC_UUID);

                    if (battleCharacteristic != null) {
                        Log.d(TAG, "対戦データ用キャラクタリスティックを見つけました");

                        // 通知を有効化
                        enableNotifications(gatt, battleCharacteristic);

                        if (callback != null) {
                            callback.onConnected();
                        }
                    } else {
                        Log.e(TAG, "対戦データ用キャラクタリスティックが見つかりません");
                        if (callback != null) {
                            callback.onError("対戦データ用キャラクタリスティックが見つかりません");
                        }
                    }
                } else {
                    Log.e(TAG, "対象のサービスが見つかりません");
                    if (callback != null) {
                        callback.onError("対象のサービスが見つかりません");
                    }
                }
            } else {
                Log.e(TAG, "サービスの探索に失敗しました: " + status);
                if (callback != null) {
                    callback.onError("サービスの探索に失敗しました: " + status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // データを受信
            byte[] value = characteristic.getValue();
            if (value != null) {
                String receivedData = new String(value, StandardCharsets.UTF_8);
                Log.d(TAG, "データを受信しました: " + receivedData);

                if (callback != null) {
                    callback.onDataReceived(receivedData);
                }
            }
        }
    };

    /**
     * 通知を有効化する
     */
    @SuppressLint("MissingPermission")
    private void enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        boolean success = gatt.setCharacteristicNotification(characteristic, true);

        if (success) {
            Log.d(TAG, "通知の有効化に成功しました");
        } else {
            Log.e(TAG, "通知の有効化に失敗しました");
            if (callback != null) {
                callback.onError("通知の有効化に失敗しました");
            }
        }
    }

    /**
     * データを送信する
     * @param data 送信するデータ（JSON文字列）
     * @return 送信成功時はtrue
     */
    @SuppressLint("MissingPermission")
    public boolean sendData(String data) {
        if (bluetoothGatt == null || battleCharacteristic == null || !isConnected) {
            Log.e(TAG, "データ送信に必要なオブジェクトが初期化されていないか、接続されていません");
            return false;
        }

        try {
            // データをセット
            battleCharacteristic.setValue(data.getBytes(StandardCharsets.UTF_8));

            // データを書き込む
            boolean success = bluetoothGatt.writeCharacteristic(battleCharacteristic);

            if (success) {
                Log.d(TAG, "データ送信に成功しました: " + data);
            } else {
                Log.e(TAG, "データ送信に失敗しました");
            }

            return success;
        } catch (Exception e) {
            Log.e(TAG, "データ送信中にエラーが発生しました", e);
            if (callback != null) {
                callback.onError("データ送信中にエラーが発生しました: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * 接続を閉じる（リソースの解放）
     */
    @SuppressLint("MissingPermission")
    public void close() {
        stopScan();

        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        battleCharacteristic = null;
        isConnected = false;

        Log.d(TAG, "接続を閉じました");
    }

    /**
     * 接続状態を返す
     * @return 接続中の場合はtrue
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * スキャン状態を返す
     * @return スキャン中の場合はtrue
     */
    public boolean isScanning() {
        return isScanning;
    }
}