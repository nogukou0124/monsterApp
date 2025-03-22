package com.example.monsterapp.model.entity.battle;

public enum BattleStatus {
    NORMAL, // 通常時
    READY_NPC_BATTLE, // NPC対戦準備中
    NPC_BATTLE_START, // NPC対戦開始// NPC対戦中,
    READY_BLE_BATTLE, // BLE対戦準備中
    BLE_BATTLE_START, // BLE対戦開始
    ATTACKING, // 攻撃中
    ATTACKED, // 被攻撃中
    WIN, // 対戦勝利
    LOSE, // 対戦敗北時
}
