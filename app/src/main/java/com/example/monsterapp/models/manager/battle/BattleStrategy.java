package com.example.monsterapp.models.manager.battle;

/**
 * 対戦方式を管理する定義するインタフェース
 */
public interface BattleStrategy {
    abstract public void prepare();
    abstract public void startBattle();
    abstract public void endBattle();
}
