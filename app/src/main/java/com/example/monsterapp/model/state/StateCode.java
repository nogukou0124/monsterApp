package com.example.monsterapp.model.state;

/**
 * Stateコード
 */
public enum StateCode {
    // 永続状態
    NORMAL,
    SICK,
    DEATH,
    SLEEP,

    // 一時状態
    JOY,
    SAD,
    ATTACK,
    ATTACKED,
    MEAL,
    DENY,
}
