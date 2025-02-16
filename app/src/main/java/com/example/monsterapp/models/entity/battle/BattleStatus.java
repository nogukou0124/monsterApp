package com.example.monsterapp.models.entity.battle;

public enum BattleStatus {
    IDLE,          // 待機中（対戦未開始）
    CONNECTING,    // 対戦相手を探している（スキャン & アドバタイズ）
    MATCHED,       // 対戦相手が見つかった（対戦準備完了）
    ATTACK,        // 攻撃
    ATTACKED,      // 被攻撃
    TIMED_OUT,     // 接続タイムアウト
    VICTORY,       // 勝利
    LOSE,          // 敗北
    ERROR          // エラー発生
}
