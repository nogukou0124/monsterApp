package com.example.monsterapp.models.manager.state;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.entity.monster.state.permanentState.DeathState;
import com.example.monsterapp.models.entity.monster.state.permanentState.NormalState;
import com.example.monsterapp.models.entity.monster.state.permanentState.SickState;
import com.example.monsterapp.models.entity.monster.state.permanentState.SleepState;
import com.example.monsterapp.models.entity.monster.state.temporaryState.TemporaryState;
import com.example.monsterapp.models.manager.MonsterManager;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;

import java.util.Timer;
import java.util.TimerTask;

/**
 * モンスターステートマシンクラス
 */
public class MonsterStateMachine {
    /** Manager */
    @NonNull MonsterManager monsterManager;
    /** state machine */
    @NonNull StateMachine stateMachine = new StateMachine();
    /** 時間遷移タスク */
    @Nullable Timer timer = null;

    /**
     * コンストラクタ
     */
    public MonsterStateMachine(@NonNull MonsterManager monsterManager) {
        this.monsterManager = monsterManager;
        // state
        stateMachine.addState(new NormalState(stateMachine, StateCode.NORMAL));
        stateMachine.addState(new SickState(stateMachine, StateCode.SICK));
        stateMachine.addState(new DeathState(stateMachine, StateCode.DEATH));
        stateMachine.addState(new TemporaryState(stateMachine, StateCode.JOY));
        stateMachine.addState(new TemporaryState(stateMachine, StateCode.SAD));
        stateMachine.addState(new TemporaryState(stateMachine, StateCode.DENY));
        stateMachine.addState(new TemporaryState(stateMachine, StateCode.MEAL));
        stateMachine.addState(new TemporaryState(stateMachine, StateCode.ATTACK));
        stateMachine.addState(new TemporaryState(stateMachine, StateCode.ATTACKED));
        stateMachine.addState(new SleepState(stateMachine, StateCode.SLEEP));
    }

    /**
     * 状態遷移の開始
     * @param initialStateCode 最初の状態コード
     */
    public void start(@NonNull StateCode initialStateCode) {
        stateMachine.setCurrentState(initialStateCode);
        timer = new Timer();
        final int ONE_SECOND = 1000;
        final int SECOND_PER_MINUTE = 60;
        final int TIMER_MINUTE = 15;

        // モンスターの状態は15分単位で変化する
        // 15分毎に状態遷移の有無を確認するタスクを実行する
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("TimerTask", "Task executed at: " + System.currentTimeMillis());
                stateMachine.handleEvent(new Event(EventCode.TIME));

            }
        }, 0, TIMER_MINUTE * SECOND_PER_MINUTE * ONE_SECOND);
    }

    /**
     * クリア関数
     */
    public void clear() {
        stateMachine.clear();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * イベント処理
     * @param event イベント
     */
    public void handleEvent(@NonNull Event event) {
        new Thread(() -> {
            try {
                stateMachine.handleEvent(event);
                onStateChanged();

                // ５秒間スリープ
                Thread.sleep(5000);
                stateMachine.handleEvent(new Event(EventCode.RETURN));
                onStateChanged();
            } catch (InterruptedException e) {
                Log.d("handle event interruptedException", e.toString());
            }
        }).start();
    }

    /**
     * コールバック処理（状態変更時）
     */
    private void onStateChanged() {
        State newState = stateMachine.getState(0);
        if (newState != null) {
            monsterManager.updateState(newState);
        }
    }
}