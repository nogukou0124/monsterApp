package com.example.monsterapp.util.callback;

import com.example.monsterapp.model.entity.battle.BattleType;
import com.example.monsterapp.model.manager.battle.BattleStatus;
import com.example.monsterapp.util.Event.Event;

public interface BattleEventListener {
    /**
     * 対戦のステータスが更新された際の処理
     * @param newBattleStatus 新しい対戦ステータス
     */
    public abstract void onChangedBattleStatus(BattleStatus newBattleStatus);
}
