package com.example.monsterapp.model.manager.battle.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.util.ble.BleConstants;
import com.example.monsterapp.util.callback.BleCallback;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * BLEサーバー機能を提供するクラス
 * 1. GATTサーバーを起動して接続を待ち受ける
 * 2. クライアントからのデータを受信してコールバックする
 * 3. クライアントにデータを送信する
 */
public class BleServer {
    // タグ（ログ出力用）
    private static final String TAG = "BleServer";

    // GATTサーバー
    @Nullable private BluetoothGattServer gattServer;

    // サービスとキャラクタリスティック
    @Nullable private BluetoothGattService service;
    @Nullable private BluetoothGattCharacteristic battleCharacteristic;
    @Nullable private BluetoothGattCharacteristic connectionCharacteristic;

    // コールバック
    @Nullable private BleCallback callback;

    // 接続デバイス
    @Nullable private BluetoothDevice connectedDevice;

    /**
     * コンストラクタ
     * @param context Androidコンテキスト
     * @param callback BLE通信のコールバック
     */
    @SuppressLint("MissingPermission")
    public BleServer(@NonNull Context context, @Nullable BleCallback callback) {
        this.callback = callback;

        try {
            // BluetoothManagerの取得
            BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager == null) {
                Log.e(TAG, "BluetoothManagerを取得できませんでした");
                if (callback != null) {
                    callback.onError("BluetoothManagerを取得できませんでした");
                }
                return;
            }

            // GATTサーバーの初期化
            gattServer = bluetoothManager.openGattServer(context, gattServerCallback);

            if (gattServer == null) {
                Log.e(TAG, "GATTサーバーを作成できませんでした");
                if (callback != null) {
                    callback.onError("GATTサーバーを作成できませんでした");
                }
                return;
            }

            // サービスの作成
            service = new BluetoothGattService(
                    BleConstants.SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            // 対戦データ用キャラクタリスティックの作成
            battleCharacteristic = new BluetoothGattCharacteristic(
                    BleConstants.BATTLE_CHARACTERISTIC_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_WRITE |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ |
                            BluetoothGattCharacteristic.PERMISSION_WRITE);

            // 接続確認用キャラクタリスティックの作成
            connectionCharacteristic = new BluetoothGattCharacteristic(
                    BleConstants.CONNECTION_CHARACTERISTIC_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_READ |
                            BluetoothGattCharacteristic.PERMISSION_WRITE);

            // サービスにキャラクタリスティックを追加
            service.addCharacteristic(battleCharacteristic);
            service.addCharacteristic(connectionCharacteristic);

            // サーバーにサービスを追加
            boolean success = gattServer.addService(service);

            if (success) {
                Log.d(TAG, "GATTサーバーの初期化に成功しました");
            } else {
                Log.e(TAG, "サービスをGATTサーバーに追加できませんでした");
                if (callback != null) {
                    callback.onError("GATTサーバーの初期化に失敗しました");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "GATTサーバーの初期化中にエラーが発生しました", e);
            if (callback != null) {
                callback.onError("GATTサーバーの初期化中にエラーが発生しました: " + e.getMessage());
            }
        }
    }

    /**
     * GATTサーバーコールバック
     * クライアントからの接続や要求に応答するための処理を定義
     */
    private final BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "デバイスが接続されました: " + device.getAddress());
                    connectedDevice = device;

                    if (callback != null) {
                        callback.onConnected();
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "デバイスが切断されました: " + device.getAddress());
                    connectedDevice = null;

                    if (callback != null) {
                        callback.onDisconnected();
                    }
                }
            } else {
                Log.e(TAG, "接続状態の変更中にエラーが発生しました: " + status);
                connectedDevice = null;

                if (callback != null) {
                    callback.onError("接続状態の変更中にエラーが発生しました: " + status);
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                                int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "読み取りリクエストを受信しました");

            if (gattServer == null) return;

            // データを返信（今回は空文字）
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    "".getBytes(StandardCharsets.UTF_8));
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            if (gattServer == null) return;

            // レスポンスの送信
            if (responseNeeded) {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
            }

            // キャラクタリスティックのUUIDに応じた処理
            UUID uuid = characteristic.getUuid();

            if (uuid.equals(BleConstants.BATTLE_CHARACTERISTIC_UUID)) {
                // バトルデータを受信
                String receivedData = new String(value, StandardCharsets.UTF_8);
                Log.d(TAG, "バトルデータを受信しました: " + receivedData);

                if (callback != null) {
                    callback.onDataReceived(receivedData);
                }
            } else if (uuid.equals(BleConstants.CONNECTION_CHARACTERISTIC_UUID)) {
                // 接続確認データを受信
                Log.d(TAG, "接続確認データを受信しました");
            }
        }
    };

    /**
     * データを送信する
     * @param data 送信するデータ（JSON文字列）
     * @return 送信成功時はtrue
     */
    @SuppressLint("MissingPermission")
    public boolean sendData(String data) {
        if (gattServer == null || battleCharacteristic == null || connectedDevice == null) {
            Log.e(TAG, "データ送信に必要なオブジェクトが初期化されていません");
            return false;
        }

        try {
            // データをセット
            battleCharacteristic.setValue(data.getBytes(StandardCharsets.UTF_8));

            // クライアントに通知
            boolean success = gattServer.notifyCharacteristicChanged(
                    connectedDevice, battleCharacteristic, false);

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
     * サーバーを閉じる（リソースの解放）
     */
    @SuppressLint("MissingPermission")
    public void close() {
        if (gattServer != null) {
            gattServer.close();
            gattServer = null;
        }

        service = null;
        battleCharacteristic = null;
        connectionCharacteristic = null;
        connectedDevice = null;

        Log.d(TAG, "GATTサーバーを閉じました");
    }
}