package com.example.monsterapp.model.manager.state;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.permanent.DeathState;
import com.example.monsterapp.model.state.permanent.NormalState;
import com.example.monsterapp.model.state.permanent.SickState;
import com.example.monsterapp.model.state.permanent.SleepState;
import com.example.monsterapp.model.state.temporary.AttackState;
import com.example.monsterapp.model.state.temporary.AttackedState;
import com.example.monsterapp.model.state.temporary.DenyState;
import com.example.monsterapp.model.state.temporary.JoyState;
import com.example.monsterapp.model.state.temporary.MealState;
import com.example.monsterapp.model.state.temporary.SadState;
import com.example.monsterapp.model.manager.MonsterManager;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;
import com.example.monsterapp.util.callback.StateEventListener;
import com.example.monsterapp.util.state.StateUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * モンスターステートマシンクラス
 */
public class MonsterStateMachine {
    /** イベントリスナー */
    @NonNull StateEventListener stateEventListener;
    /** state machine */
    @NonNull StateMachine stateMachine;
    /** 時間タスクスケジューラ */
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * コンストラクタ
     */
    public MonsterStateMachine(@NonNull StateEventListener stateEventListener) {
        this.stateEventListener = stateEventListener;
        // state
        stateMachine = new StateMachine();
        stateMachine.addState(new NormalState(stateMachine, StateCode.NORMAL));
        stateMachine.addState(new SickState(stateMachine, StateCode.SICK));
        stateMachine.addState(new DeathState(stateMachine, StateCode.DEATH));
        stateMachine.addState(new SleepState(stateMachine, StateCode.SLEEP));
        stateMachine.addState(new JoyState(stateMachine, StateCode.JOY));
        stateMachine.addState(new SadState(stateMachine, StateCode.SAD));
        stateMachine.addState(new DenyState(stateMachine, StateCode.DENY));
        stateMachine.addState(new MealState(stateMachine, StateCode.MEAL));
        stateMachine.addState(new AttackState(stateMachine, StateCode.ATTACK));
        stateMachine.addState(new AttackedState(stateMachine, StateCode.ATTACKED));
    }

    /**
     * 状態遷移の開始
     * @param initialStateCode 最初の状態コード
     */
    public void start(@NonNull StateCode initialStateCode) {
        Log.d("state machine start", "initial state is " + initialStateCode);
        stateMachine.setCurrentState(initialStateCode);
        scheduler.scheduleWithFixedDelay(() -> {
            stateEventListener.onTimeEvent(new Event(EventCode.TIME));
        }, 0, StateUtils.TIME_EVENT_DURATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * クリア関数
     */
    public void clear() {
        State currentState = stateMachine.getCurrentState();
        // 一時状態の場合は、永続状態に戻してから終了する
        if (currentState != null && currentState.isTemporary()) {
            State lastPermanentState = stateMachine.getPreState();
            if (lastPermanentState != null) {
                stateMachine.transition(lastPermanentState.stateCode);
                stateEventListener.onUpdatedState(currentState, lastPermanentState);
            }
        }
        stateMachine.clear();
        scheduler.shutdown();
    }

    /**
     * イベント処理
     * @param event イベント
     */
    public void handleEvent(@NonNull Event event) {
        stateMachine.handleEvent(event);

        // モンスターの状態を更新する
        State oldState = stateMachine.getPreState();
        State newState = stateMachine.getCurrentState();

        // nullは異常系のため、Managerに通知
        if (newState == null || oldState == null) {
            throw new NullPointerException("状態遷移イベントに失敗しました");
        }

        // 通知
        stateEventListener.onUpdatedState(oldState, newState);

        // 一時状態であれば、5秒間継続し前の状態に戻る
        if (newState.isTemporary()) {
            new Thread(() -> {
                try {
                    Thread.sleep(StateUtils.TEMPORARY_DURATION_TIME);
                    stateMachine.transition(oldState.stateCode);
                    stateEventListener.onUpdatedState(newState, oldState);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

}