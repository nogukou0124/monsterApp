package com.example.monsterapp.models.entity.state;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.monsterapp.utils.TimeConstants;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.models.manager.state.StateMachine;

import java.time.LocalTime;

/**
 * Stateクラス
 */
@SuppressLint("NewApi")
public abstract class State {
    /** 状態コード */
    public StateCode stateCode;

    /** StateMachine */
    public StateMachine stateMachine;

    /**
     * コンストラクタ
     * @param stateMachine StateMachine
     * @param stateCode 状態コード
     */
    public State(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        this.stateMachine = stateMachine;
        this.stateCode = stateCode;
    }

    /**
     * 他状態からの遷移時処理
     */
    public void onEnter() {
        Log.d("onEnter Event", String.valueOf(this.stateCode));
    }

    /**
     * 他状態への遷移時処理
     */
    public void onExit() {
        Log.d("onExit Event", String.valueOf(this.stateCode));
    }

    /**
     * 状態遷移処理
     * @param nextStateCode 次の状態コード
     */
    public void onTransition(StateCode nextStateCode) {
        Log.d("onEnter Event", String.valueOf(this.stateCode));
        stateMachine.transition(nextStateCode);
    }

    /**
     * イベント処理関数
     * @param event イベント
     */
    public void handleEvent(@NonNull Event event) {
        switch (event.eventCode) {
            case RESET:
                onTransition(StateCode.NORMAL);
                break;
            case ATTACK:
                onTransition(StateCode.ATTACK);
                break;
            case ATTACKED:
                onTransition(StateCode.ATTACKED);
                break;
            case VICTORY:
                onTransition(StateCode.JOY);
                break;
            case LOSE:
                onTransition(StateCode.SAD);
                break;
            case TIME:
                // 活動時間外になったら睡眠状態に遷移
                LocalTime now = LocalTime.now();
                if (now.isBefore(TimeConstants.ACTIVITY_START_TIME) || now.isAfter(TimeConstants.ACTIVITY_END_TIME)) {
                    stateMachine.transition(StateCode.SLEEP);
                    break;
                }
        }
    }
}
