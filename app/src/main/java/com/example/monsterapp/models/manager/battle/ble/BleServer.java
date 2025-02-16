package com.example.monsterapp.models.manager.battle.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.monsterapp.utils.BleConstants;

public class BleServer {
    @Nullable private BluetoothGattServer gattServer;
    @Nullable private BluetoothGattService service;
    @Nullable private BluetoothGattCharacteristic characteristic;
    @Nullable private BluetoothAdapter adapter;

    @SuppressLint("MissingPermission")
    public BleServer(BluetoothAdapter adapter, Context context) {
        this.adapter = adapter;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback);

        service = new BluetoothGattService(BleConstants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        characteristic = new BluetoothGattCharacteristic(
                BleConstants.CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PROPERTY_WRITE
        );

        service.addCharacteristic(characteristic);
        gattServer.addService(service);
    }

    private final BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            String receivedData = new String(value);
            Log.d("BLE", "受信: " + receivedData);
        }
    };

    @SuppressLint("MissingPermission")
    public void sendData(String data) {
        if (gattServer == null || characteristic == null) { return; }
        characteristic.setValue(data);
        gattServer.notifyCharacteristicChanged(null, characteristic, false);
    }
}
