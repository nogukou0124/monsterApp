package com.example.monsterapp.model.manager.battle.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.entity.battle.BattleType;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.manager.battle.BattleManager;
import com.example.monsterapp.model.manager.battle.BattleStrategy;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.battle.BattleUtils;
import com.example.monsterapp.util.callback.BleCallback;
import com.google.gson.Gson;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BLEを使用した対戦戦略クラス
 * 1. BLEを使ってP2P（端末間）通信を実現
 * 2. ホスト・ゲストどちらにもなれるよう設計
 * 3. 対戦データをJSONでやり取り
 */
public class BleBattleStrategy implements BattleStrategy {
    // タグ（ログ出力用）
    private static final String TAG = "BleBattleStrategy";

    // バトル管理
    @NonNull private final BattleManager battleManager;

    // アプリケーションコンテキスト
    @NonNull private final Context context;

    // Bluetooth関連
    @Nullable private final BluetoothAdapter bluetoothAdapter;
    @Nullable private BleServer bleServer;
    @Nullable private BleAdvertiser bleAdvertiser;
    @Nullable private BleScanner bleScanner;

    // 接続と役割状態
    private boolean isConnected = false;
    private boolean isHost = false;

    // ターン管理
    private final AtomicBoolean isWaitingForResponse = new AtomicBoolean(false);

    // ダメージログ
    @Nullable private String battleLog;

    // 通信データをJSON変換するためのGson
    private final Gson gson = new Gson();

    /**
     * コンストラクタ
     * @param battleManager バトル管理オブジェクト
     * @param context アプリケーションコンテキスト
     */
    public BleBattleStrategy(@NonNull BattleManager battleManager, @NonNull Context context) {
        this.battleManager = battleManager;
        this.context = context;

        // Bluetoothアダプターの取得
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager != null ?
                bluetoothManager.getAdapter() : null;

        if (bluetoothAdapter == null) {
            Log.e(TAG, "このデバイスはBluetoothをサポートしていません");
        }
    }

    /**
     * 敵モンスターを作成する
     * 実際の敵モンスターデータは接続後に相手から送られてくる
     */
    @NonNull
    @Override
    public Monster createEnemyMonster() {
        // 初期値として仮のモンスターを返す
        // 後で実際の相手のモンスターデータで上書きされる
        return new Monster(2, StateCode.NORMAL, "対戦相手のモンスター", 8, 8, 2);
    }

    /**
     * 先行/後攻を決定する
     * ホスト側が先行、ゲスト側が後攻
     */
    @Override
    public boolean decideFirstAttacker(@NonNull Monster myMonster, @NonNull Monster enemyMonster) {
        return isHost;
    }

    /**
     * 自分のターンを処理する
     * ダメージ計算して相手に送信
     */
    @Override
    public void executeMyTurn() {
        @Nullable Monster myMonster = battleManager.getMyMonster();
        @Nullable Monster enemyMonster = battleManager.getEnemyMonster();

        if (myMonster == null || enemyMonster == null || !isConnected) {
            Log.e(TAG, "モンスターが初期化されていないか、接続されていません");
            return;
        }

        // ダメージ計算
        int damage = BattleUtils.getDamage(myMonster, enemyMonster);
        enemyMonster.hp = Math.max(enemyMonster.hp - damage, 0);

        // バトルデータを作成
        BleBattleData battleData = new BleBattleData(
                damage,
                myMonster.hp,
                myMonster.name,
                myMonster.power
        );

        // JSONに変換
        String jsonData = gson.toJson(battleData);

        // 送信
        boolean success = false;
        if (isHost && bleServer != null) {
            success = bleServer.sendData(jsonData);
        } else if (!isHost && bleScanner != null) {
            success = bleScanner.sendData(jsonData);
        }

        if (success) {
            Log.d(TAG, "バトルデータを送信しました: " + jsonData);
            isWaitingForResponse.set(true);

            // バトルログを更新
            battleLog = String.format("%sの攻撃！%sに%dのダメージ！残りHP: %d",
                    myMonster.name, enemyMonster.name, damage, enemyMonster.hp);
        } else {
            Log.e(TAG, "バトルデータの送信に失敗しました");
        }
    }

    /**
     * 敵のターンを処理する
     * 相手からのデータ受信イベントで実際の処理が行われる
     */
    @Override
    public void executeEnemyTurn() {
        // 実際のダメージ処理はBleCallbackのonDataReceivedで行われる
        // このメソッドはBattleManagerから呼ばれるだけ
        Log.d(TAG, "相手のターン: データ受信待ち");
    }

    /**
     * リソースのクリーンアップ
     */
    @Override
    public void cleanUp() {
        isConnected = false;
        isWaitingForResponse.set(false);

        if (bleAdvertiser != null) {
            bleAdvertiser.stopAdvertising();
            bleAdvertiser = null;
        }

        if (bleScanner != null) {
            bleScanner.close();
            bleScanner = null;
        }

        if (bleServer != null) {
            bleServer.close();
            bleServer = null;
        }

        Log.d(TAG, "BLE対戦リソースをクリーンアップしました");
    }

    /**
     * BLE対戦を初期化する
     * この関数はcreateEnemyMonsterが呼ばれた後に実行される
     */
    public void initialize() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetoothアダプターが利用できないため、BLE対戦を開始できません");
            return;
        }

        // ランダムにホスト/ゲストを決定
        // 実際のアプリではユーザーに選択させることも可能
        isHost = new Random().nextBoolean();

        // 役割に応じたBLEコンポーネントを初期化
        if (isHost) {
            initializeAsHost();
        } else {
            initializeAsGuest();
        }
    }

    /**
     * ホスト役としてBLEを初期化
     * 1. サーバーを起動
     * 2. アドバタイズを開始
     */
    private void initializeAsHost() {
        Log.d(TAG, "ホスト役として初期化します");

        // BLEサーバーの初期化
        bleServer = new BleServer(context, bleCallback);

        // BLEアドバタイザーの初期化と開始
        bleAdvertiser = new BleAdvertiser(bluetoothAdapter, bleCallback);
        boolean success = bleAdvertiser.startAdvertising();

        if (success) {
            Log.d(TAG, "ホスト役としての初期化に成功しました。対戦相手を待っています");
        } else {
            Log.e(TAG, "ホスト役としての初期化に失敗しました");
        }
    }

    /**
     * ゲスト役としてBLEを初期化
     * 1. スキャナーを起動
     * 2. ホストデバイスを探してスキャン
     */
    private void initializeAsGuest() {
        Log.d(TAG, "ゲスト役として初期化します");

        // BLEスキャナーの初期化と開始
        bleScanner = new BleScanner(bluetoothAdapter, context, bleCallback);
        boolean success = bleScanner.startScan();

        if (success) {
            Log.d(TAG, "ゲスト役としての初期化に成功しました。ホストを探しています");
        } else {
            Log.e(TAG, "ゲスト役としての初期化に失敗しました");
        }
    }

    /**
     * BLE通信のコールバック
     */
    private final BleCallback bleCallback = new BleCallback() {
        @Override
        public void onConnected() {
            Log.d(TAG, "BLE接続が確立されました");
            isConnected = true;

            // 接続直後にモンスター情報を交換（未実装）
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "BLE接続が切断されました");
            isConnected = false;
        }

        @Override
        public void onDataReceived(String data) {
            Log.d(TAG, "データを受信しました: " + data);

            try {
                // JSONデータをパース
                BleBattleData battleData = gson.fromJson(data, BleBattleData.class);

                // 自分のモンスター取得
                Monster myMonster = battleManager.getMyMonster();
                if (myMonster == null) return;

                // ダメージ処理
                int damage = battleData.damage;
                myMonster.hp = Math.max(myMonster.hp - damage, 0);

                // バトルログを更新
                battleLog = String.format("%sの攻撃！%sに%dのダメージ！残りHP: %d",
                        battleData.monsterName, myMonster.name, damage, myMonster.hp);

                // ターン完了を通知
                isWaitingForResponse.set(false);

                Log.d(TAG, battleLog);
            } catch (Exception e) {
                Log.e(TAG, "データの解析に失敗しました", e);
            }
        }

        @Override
        public void onError(String errorMessage) {
            Log.e(TAG, "BLEエラー: " + errorMessage);
        }
    };

    /**
     * バトルログを取得
     * @return 最新のバトルログ
     */
    @Nullable
    public String getBattleLog() {
        return battleLog;
    }

    /**
     * 接続状態を取得
     * @return 接続中の場合はtrue
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 応答待ち状態を取得
     * @return 相手からの応答待ちの場合はtrue
     */
    public boolean isWaitingForResponse() {
        return isWaitingForResponse.get();
    }
}