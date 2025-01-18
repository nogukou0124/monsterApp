package com.example.monsterapp.models.manager.battle.npc;

import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.models.manager.battle.BattleManager;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;

import java.util.Random;

/**
 * NPC対戦管理クラス
 */
public class NPCBattleManager extends BattleManager {
    /** 各ターンの遅延時間（ミリ秒） */
    private static final int TURN_DELAY_MS = 6000; //

    public NPCBattleManager(Monster playerMonster) {
        super(playerMonster);
        enemyMonster = new Monster(2, StateCode.NORMAL, "NPC",10,2);
    }

    @Override
    public void startBattle() {
        // 先攻後攻を決定
        Random random = new Random();

        new Thread(() -> {
            Monster attacker = random.nextBoolean() ? playerMonster : enemyMonster;
            Monster defender = attacker == playerMonster ? enemyMonster : playerMonster;

            // 攻撃者のHPが0になるまで順番に攻撃しあう
            // while文を抜ける際、被攻撃者であったほうが勝ち
            while (attacker.hp != 0) {

                attack(attacker, defender);

                try {
                    Thread.sleep(TURN_DELAY_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Monster tmp = attacker;
                attacker = defender;
                defender = tmp;
            }

            // 勝敗判定
            if (attacker == enemyMonster) {
                onFinished(new Event(EventCode.VICTORY));
            }
            else {
                onFinished(new Event(EventCode.LOSE));
            }
        }).start();
    }
}
