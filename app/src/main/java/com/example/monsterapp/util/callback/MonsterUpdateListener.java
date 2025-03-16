package com.example.monsterapp.util.callback;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.entity.monster.Monster;

public interface MonsterUpdateListener {
    public abstract void onUpdatedMonster(@NonNull Monster newMonster);
}
