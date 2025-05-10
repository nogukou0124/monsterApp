package com.example.monsterapp.model.manager.battle.ble;

import androidx.annotation.NonNull;

/**
 * BLE通信で交換する対戦データ
 */
public class BleBattleData {
    // 与えるダメージ
    public int damage;

    // モンスターの残りHP
    public int remainingHp;

    // モンスターの名前
    @NonNull
    public String monsterName;

    // モンスターの攻撃力
    public int power;

    /**
     * 通信用のバトルデータを作成
     */
    public BleBattleData(int damage, int remainingHp, @NonNull String monsterName, int power) {
        this.damage = damage;
        this.remainingHp = remainingHp;
        this.monsterName = monsterName;
        this.power = power;
    }
}