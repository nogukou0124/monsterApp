package com.example.monsterapp.models.manager.battle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.battle.BattleStatus;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.manager.MonsterManager;
import com.example.monsterapp.models.manager.battle.npc.NPCBattleStrategy;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.battle.BattleUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 対戦管理クラス
 */
public class BattleManager {
    // State
    /** 対戦前の自分のモンスター　*/
    @Nullable private Monster preBattleMyMonster;
    /** 自分のモンスター　*/
    @Nullable private Monster myMonster;
    /** 相手のモンスター　*/
    @Nullable private Monster enemyMonster;

    /** Manager */
    @NonNull private MonsterManager monsterManager;
    /** Strategy */
    @Nullable private BattleStrategy battleStrategy;

    /** NPC対戦発生時の開始待機処理スレッド */
    @Nullable private Thread npcBattleWaitThread;


    /** 時間タスクスケジューラ */
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    /**
     * コンストラクタ
     */
    public BattleManager(@NonNull MonsterManager monsterManager) {
        this.monsterManager = monsterManager;
        executeNpcBattleScheduler();
    }

    /**
     * NPC対戦のスケジューラーを起動させる
     */
    public void executeNpcBattleScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            Log.d("TimerTask", "Task executed at: " + System.currentTimeMillis());
            battleStrategy = new NPCBattleStrategy(this);
            monsterManager.postBattleStatus(BattleStatus.NPC_BATTLE_TRIGGERED);

            // 5秒間待機したあと、NPC対戦を開始する
            npcBattleWaitThread = new Thread(() ->{
                try {
                    Thread.sleep(5000);
                    monsterManager.postBattleStatus(BattleStatus.NPC_BATTLE_START);
                    startBattle();
                } catch (InterruptedException e) {
                    Log.e("handle event interruptedException", e.toString());
                } catch (NullPointerException e) {
                    Log.e("NullPointerException", e.toString());
                }
            });
            npcBattleWaitThread.start();
        }, 5 * 1000, BattleUtils.NPC_BATTLE_DURATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * NPC対戦を中止する
     */
    public void cancelNpcBattle() {
        if (npcBattleWaitThread != null) {
            npcBattleWaitThread.interrupt();
            monsterManager.postBattleStatus(BattleStatus.IDLE);
        }
    }

    /**
     * 自分のモンスターと相手のモンスターを設定する
     */
    public void onBattleEvent(BattleStatus newBattleStatus, Monster newMyMonster) {
        monsterManager.updateMonster(newMyMonster);
    }

    /**
     * 対戦処理を進める
     */
    public void startBattle() {
        // 対戦ロジックが設定されていない場合、対戦できないため終了
        if (battleStrategy == null) {
            throw new NullPointerException("対戦方式が正しく設定されませんでした");
        }

        // 対戦対象となる自分のモンスターが見つからない場合、対戦できないため終了
        myMonster = monsterManager.getCurrentMonster();
        if (myMonster == null) {
            throw new NullPointerException("自分のモンスターが見つかりませんでした");
        }
        enemyMonster = battleStrategy.getEnemyMonster();

        // 対戦前のモンスター情報を残しておく（対戦後、元の情報に書き換えるため）
        preBattleMyMonster = new Monster(
                myMonster.uid,
                myMonster.stateCode,
                myMonster.name,
                myMonster.hp,
                myMonster.power
        );

        // 対戦処理
        battleStrategy.executeBattle(myMonster, enemyMonster);
    }

    public void endBattle() {
    }
}
