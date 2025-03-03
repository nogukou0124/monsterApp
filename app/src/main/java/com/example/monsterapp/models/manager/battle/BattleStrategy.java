package com.example.monsterapp.models.manager.battle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.Monster;

/**
 * 対戦方式を管理する定義するインタフェース
 */
public interface BattleStrategy {
    /**
     * 敵のモンスターを取得する
     * @return 敵のモンスター
     */
    @NonNull abstract public Monster getEnemyMonster();

    abstract public void executeBattle(@NonNull Monster myMonster, @NonNull Monster enemyMonster);
    abstract public void endBattle();
}
