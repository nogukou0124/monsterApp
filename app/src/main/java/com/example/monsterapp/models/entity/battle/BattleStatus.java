package com.example.monsterapp.models.entity.battle;

public enum BattleStatus {
    IDLE, // 待機中（対戦未開始）
    NPC_BATTLE_TRIGGERED, // NPC対戦発生時
    NPC_BATTLE_START, // NPC対戦開始
    UPDATING_STATE, // 状態更新中
    IN_BATTLE, // 対戦中
    BEFORE_NPC_BATTLE,
    CONNECTING,    // 対戦相手を探している（スキャン & アドバタイズ）
    MATCHED,       // 対戦相手が見つかった（対戦準備完了）
    ATTACK,        // 攻撃
    ATTACKED,      // 被攻撃
    TIMED_OUT,     // 接続タイムアウト
    VICTORY,       // 勝利
    LOSE,          // 敗北
    ERROR          // エラー発生
}
