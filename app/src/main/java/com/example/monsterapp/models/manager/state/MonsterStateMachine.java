package com.example.monsterapp.models.manager.state;

import androidx.annotation.NonNull;

import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.entity.monster.state.permanent.DeathState;
import com.example.monsterapp.models.entity.monster.state.permanent.NormalState;
import com.example.monsterapp.models.entity.monster.state.permanent.SickState;
import com.example.monsterapp.models.entity.monster.state.permanent.SleepState;
import com.example.monsterapp.models.entity.monster.state.temporary.AttackState;
import com.example.monsterapp.models.entity.monster.state.temporary.AttackedState;
import com.example.monsterapp.models.entity.monster.state.temporary.DenyState;
import com.example.monsterapp.models.entity.monster.state.temporary.JoyState;
import com.example.monsterapp.models.entity.monster.state.temporary.MealState;
import com.example.monsterapp.models.entity.monster.state.temporary.SadState;
import com.example.monsterapp.models.manager.MonsterManager;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.utils.state.StateUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * モンスターステートマシンクラス
 */
public class MonsterStateMachine {
    /** Manager */
    @NonNull MonsterManager monsterManager;
    /** state machine */
    @NonNull StateMachine stateMachine;
    /** 時間タスクスケジューラ */
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * コンストラクタ
     */
    public MonsterStateMachine(@NonNull MonsterManager monsterManager) {
        this.monsterManager = monsterManager;
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
        stateMachine.setCurrentState(initialStateCode);
        scheduler.scheduleWithFixedDelay(() -> {
            stateMachine.handleEvent(new Event(EventCode.TIME));
        }, 0, StateUtils.TIME_EVENT_DURATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * クリア関数
     */
    public void clear() {
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
        State currentState = stateMachine.getCurrentState();

        // nullは異常系のため、Managerに通知
        if (currentState == null) {
            throw new NullPointerException("状態遷移イベントに失敗しました");
        }

        updateState(currentState);

        // 一時状態であれば、5秒間継続し前の状態に戻る
        if (currentState.isTemporary()) {
            new Thread(() -> {
                try {
                    Thread.sleep(StateUtils.TEMPORARY_DURATION_TIME);
                    State preState = stateMachine.getPreState();

                    if (preState == null) {
                        throw new NullPointerException("状態遷移イベントに失敗しました");
                    }
                    stateMachine.transition(preState.stateCode);
                    updateState(preState);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    /**
     * モンスターの状態を更新するメソッド
     * @param newState 新しい状態
     */
    public void updateState(State newState) {
        Monster monster = monsterManager.getCurrentMonster();
        if (monster == null) {
            throw new NullPointerException("モンスターが存在しません");
        }
        monster.stateCode = newState.stateCode;
        monsterManager.updateMonster(monster);
    }
}