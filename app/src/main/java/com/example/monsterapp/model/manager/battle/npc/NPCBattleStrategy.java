package com.example.monsterapp.model.manager.battle.npc;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.manager.battle.BattleManager;
import com.example.monsterapp.model.manager.battle.BattleStrategy;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;
import com.example.monsterapp.util.battle.BattleUtils;

import java.util.Random;

import hilt_aggregated_deps._dagger_hilt_android_internal_managers_ViewComponentManager_ViewWithFragmentComponentBuilderEntryPoint;

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
    @Override public Monster createEnemyMonster() {
        return new Monster(2, StateCode.NORMAL, "NPC Monster",10, 10,2);
    }

    @Override
    public boolean decideFirstAttacker(@NonNull Monster myMonster, @NonNull Monster enemyMonster) {
        // 先攻後攻を決定する
        return new Random().nextBoolean();
    }

    @Override
    public void executeMyTurn() {
        Log.d("battle event", "my turn");
        @Nullable Monster myMonster = battleManager.getMyMonster();
        @Nullable Monster enemyMonster = battleManager.getEnemyMonster();
        if (myMonster == null || enemyMonster == null) { return; }

        int damage = BattleUtils.getDamage(myMonster, enemyMonster);
        enemyMonster.hp = Math.max(enemyMonster.hp - damage, 0);
        Log.d("battle event", "myMonster Attack! myMonsterHP:" + myMonster.hp + " enemyMonster HP:" + enemyMonster.hp);

        if (enemyMonster.hp == 0) {
            battleManager.endBattle(true);
            return;
        }

        try {
            Thread.sleep(BattleUtils.NPC_BATTLE_TRIGGER_TO_START_TIME);
            battleManager.executeTurn(false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void executeEnemyTurn() {
        Log.d("battle event", "enemy turn");
        @Nullable Monster myMonster = battleManager.getMyMonster();
        @Nullable Monster enemyMonster = battleManager.getEnemyMonster();
        if (myMonster == null || enemyMonster == null) { return; }

        int damage = BattleUtils.getDamage(enemyMonster, myMonster);
        myMonster.hp = Math.max(myMonster.hp - damage, 0);
        Log.d("battle event", "myMonster Attacked! myMonsterHP:" + myMonster.hp + " enemyMonster HP:" + enemyMonster.hp);

        if (myMonster.hp == 0) {
            battleManager.endBattle(false);
            return;
        }

        try {
            Thread.sleep(BattleUtils.NPC_BATTLE_TRIGGER_TO_START_TIME);
            battleManager.executeTurn(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanUp() {

    }
}
