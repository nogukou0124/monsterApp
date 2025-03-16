package com.example.monsterapp.model.manager.battle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.entity.battle.BattleType;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.manager.battle.ble.BleBattleStrategy;
import com.example.monsterapp.model.manager.battle.npc.NPCBattleStrategy;
import com.example.monsterapp.util.battle.BattleUtils;
import com.example.monsterapp.util.callback.BattleEventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 対戦管理クラス
 */
public class BattleManager {
    // State
    /** 自分のモンスター　*/
    @Nullable private Monster myMonster;
    /** 相手のモンスター　*/
    @Nullable private Monster enemyMonster;

    /** EventListener */
    @NonNull private final BattleEventListener battleEventListener;
    /** Strategy */
    @Nullable private BattleStrategy battleStrategy;

    /** NPC対戦発生時の開始待機処理スレッド */
    @Nullable private Thread npcBattleWaitThread;

    /** 時間タスクスケジューラ */
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    /**
     * コンストラクタ
     */
    public BattleManager(@NonNull BattleEventListener battleEventListener) {
        this.battleEventListener = battleEventListener;
        executeNpcBattleScheduler();
    }

    /**
     * NPC対戦のスケジューラーを起動させる
     */
    public void executeNpcBattleScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            Log.d("TimerTask", "Task executed at: " + System.currentTimeMillis());
            // NPC対戦の発生を通知
            battleEventListener.onChangedBattleStatus(BattleStatus.READY_NPC_BATTLE);

            // 5秒間待機したあと、NPC対戦を開始する
            npcBattleWaitThread = new Thread(() ->{
                try {
                    Thread.sleep(BattleUtils.NPC_BATTLE_TRIGGER_TO_START_TIME);
                    battleEventListener.onChangedBattleStatus(BattleStatus.NPC_BATTLE_START);
                } catch (InterruptedException e) {
                    Log.e("handle event interruptedException", e.toString());
                } catch (NullPointerException e) {
                    Log.e("NullPointerException", e.toString());
                }
            });
            npcBattleWaitThread.start();
        }, 5, BattleUtils.NPC_BATTLE_DURATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * NPC対戦を中止する
     */
    public void cancelNpcBattle() {
        if (npcBattleWaitThread != null) {
            npcBattleWaitThread.interrupt();
        }
    }

    /**
     * 対戦を開始する
     */
    public void startBattle(@NonNull BattleType battleType, @Nullable Monster myMonster) {
        if (myMonster == null) { return; }

        Log.d("battle event", "start battle!:" + battleType.name());
        if (battleType == BattleType.NPC) {
            battleStrategy = new NPCBattleStrategy(this);
        } else if (battleType == BattleType.BLE) {
            battleStrategy = new BleBattleStrategy(this);
        } else {
            return;
        }

        this.myMonster = myMonster;
        this.enemyMonster = battleStrategy.createEnemyMonster();

        // 対戦処理
        boolean isMyTurn = battleStrategy.decideFirstAttacker(this.myMonster, this.enemyMonster);
        executeTurn(isMyTurn);
    }

    public void executeTurn(boolean isMyTurn) {
        if (battleStrategy == null || myMonster == null || enemyMonster == null) { return; }

        //　対戦終了チェック
        if (myMonster.hp == 0) {
            endBattle(false);
            return;
        } else if (enemyMonster.hp == 0) {
            endBattle(true);
            return;
        }

        if (isMyTurn) {
            Log.d("battle event ", "attack");
            battleStrategy.executeMyTurn();
            battleEventListener.onChangedBattleStatus(BattleStatus.ATTACKING);
        } else {
            Log.d("battle event ", "attack");
            battleStrategy.executeEnemyTurn();
            battleEventListener.onChangedBattleStatus(BattleStatus.ATTACKED);
        }
    }

    /**
     * 対戦を終了する
     */
    public void endBattle(boolean isWin) {
        if (isWin) {
            battleEventListener.onChangedBattleStatus(BattleStatus.WIN);
        } else {
            battleEventListener.onChangedBattleStatus(BattleStatus.LOSE);
        }
        if (battleStrategy != null) {
            battleStrategy.cleanUp();
        }
        battleEventListener.onChangedBattleStatus(BattleStatus.NORMAL);
    }

    @Nullable
    public Monster getMyMonster() {
        return myMonster;
    }

    @Nullable
    public Monster getEnemyMonster() {
        return enemyMonster;
    }
}
