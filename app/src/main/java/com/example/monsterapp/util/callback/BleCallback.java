package com.example.monsterapp.util.callback;

/**
 * BLE通信のコールバックインターフェース
 */
public interface BleCallback {
    /**
     * BLE接続が確立されたときに呼ばれる
     */
    void onConnected();

    /**
     * BLE接続が切断されたときに呼ばれる
     */
    void onDisconnected();

    /**
     * データを受信したときに呼ばれる
     * @param data 受信したJSONデータ
     */
    void onDataReceived(String data);

    /**
     * エラーが発生したときに呼ばれる
     * @param errorMessage エラーメッセージ
     */
    void onError(String errorMessage);
}