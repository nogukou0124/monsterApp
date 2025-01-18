package com.example.monsterapp.models.entity.battle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.utils.Event.Event;

/**
 * 対戦データ
 */
public class BattleData {
    @NonNull public Monster monster;
    @Nullable public Event event;

    public BattleData(@NonNull Monster monster) {
        this.monster = monster;
    }

    public BattleData(@NonNull Monster monster, @Nullable Event event) {
        this.monster = monster;
        this.event = event;
    }
}
