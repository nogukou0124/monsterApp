package com.example.monsterapp.models.manager.battle.npc;

import androidx.annotation.NonNull;

import com.example.monsterapp.models.entity.battle.BattleStatus;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.manager.battle.BattleManager;
import com.example.monsterapp.models.manager.battle.BattleStrategy;
import com.example.monsterapp.utils.battle.BattleUtils;
import com.example.monsterapp.utils.state.StateUtils;

import java.util.Random;

/**
 * NPC対戦管理クラス
 */
public class NPCBattleStrategy implements BattleStrategy {
    @NonNull BattleManager battleManager;

    /**
     * コンストラクタ
     */
    public NPCBattleStrategy(@NonNull BattleManager battleManager) {
        this.battleManager = battleManager;
    }

    @NonNull
    @Override public Monster getEnemyMonster() {
        return new Monster(2, StateCode.NORMAL, "NPC Monster",10,2);
    }

    @Override
    public void executeBattle(@NonNull Monster myMonster, @NonNull Monster enemyMonster) {
        new Thread(() -> {
            try {
                boolean isMyPlayerFirst = new Random().nextBoolean();
                Monster firstMonster = isMyPlayerFirst ? myMonster : enemyMonster;
                Monster secondMonster = isMyPlayerFirst ? enemyMonster : myMonster;
                while(myMonster.hp != 0 && enemyMonster.hp != 0) {
                    // 先攻の攻撃
                    secondMonster.hp = Math.max(secondMonster.hp - BattleUtils.getDamage(firstMonster, secondMonster),0);
                    battleManager.onBattleEvent(isMyPlayerFirst ? BattleStatus.ATTACK : BattleStatus.ATTACKED, myMonster);
                    Thread.sleep(StateUtils.TEMPORARY_DURATION_TIME);

                    // 後攻の攻撃
                    firstMonster.hp = Math.max(firstMonster.hp - BattleUtils.getDamage(secondMonster, firstMonster),0);
                    battleManager.onBattleEvent(isMyPlayerFirst ? BattleStatus.ATTACKED : BattleStatus.ATTACK, myMonster);
                    Thread.sleep(StateUtils.TEMPORARY_DURATION_TIME);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void endBattle() {

    }
}
