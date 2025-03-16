package com.example.monsterapp.model.manager.battle.ble;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.manager.battle.BattleManager;
import com.example.monsterapp.model.manager.battle.BattleStrategy;

public class BleBattleStrategy implements BattleStrategy {

    public BleBattleStrategy(BattleManager battleManager) {

    }

    @NonNull
    @Override
    public Monster createEnemyMonster() {
        return null;
    }

    @Override
    public boolean decideFirstAttacker(@NonNull Monster myMonster, @NonNull Monster enemyMonster) {
        return false;
    }

    @Override
    public void executeMyTurn() {

    }

    @Override
    public void executeEnemyTurn() {

    }

    @Override
    public void cleanUp() {

    }
}
