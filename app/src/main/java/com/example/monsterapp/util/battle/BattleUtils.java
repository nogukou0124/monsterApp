package com.example.monsterapp.util.battle;

import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.state.StateCode;

import java.util.Random;

/**
 * BattleのUtilクラス
 */
public class BattleUtils {
    /** NPC対戦の間隔 */
    public static final long NPC_BATTLE_DURATION_TIME = 12 * 60 * 60 * 1000;
    /** NPC対戦発生から対戦開始までの時間 */
    public static final long NPC_BATTLE_TRIGGER_TO_START_TIME = 7 * 1000;

    /**
     * 攻撃者が被攻撃者に与えるダメージを計算する
     * @param attacker　攻撃者
     * @param defender　被攻撃者
     * @return ダメージ
     */
    public static int getDamage(Monster attacker, Monster defender) {
        Random random = new Random();
        // 命中率は基本80％
        // ただし、攻撃者が病気の場合は20%低下、被攻撃者が病気の場合15%増加
        double hitRate = 0.8;
        hitRate = attacker.stateCode == StateCode.SICK ? hitRate - 0.2 : hitRate;
        hitRate = defender.stateCode == StateCode.SICK ? hitRate + 0.15 : hitRate;

        // 命中した場合は、攻撃者の攻撃力分ダメージを与える。
        // 攻撃を外した場合は、0ダメージ。
        if (random.nextDouble() <= hitRate) {
            return attacker.power;
        }
        else {
            return 0;
        }
    }
}
