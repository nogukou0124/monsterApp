package com.example.monsterapp.model.manager.battle;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.entity.monster.Monster;

/**
 * 対戦方式を管理する定義するインタフェース
 */
public interface BattleStrategy {
    /**
     * 敵のモンスターを取得する
     * @return 敵のモンスター
     */
    @NonNull abstract public Monster createEnemyMonster();

    abstract public boolean decideFirstAttacker(@NonNull Monster myMonster, @NonNull Monster enemyMonster);
    abstract public void executeMyTurn();
    abstract public void executeEnemyTurn();
    abstract public void cleanUp();
}
