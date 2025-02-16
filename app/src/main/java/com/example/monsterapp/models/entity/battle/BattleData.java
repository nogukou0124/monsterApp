package com.example.monsterapp.models.entity.battle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.utils.Event.Event;

/**
 * 対戦データ
 */
public class BattleData {
    @NonNull public BattleStatus battleStatus;
    public int hp;

    public BattleData(@NonNull BattleStatus battleStatus, int hp) {
        this.battleStatus = battleStatus;
        this.hp = hp;
    }
}
