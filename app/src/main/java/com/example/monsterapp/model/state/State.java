package com.example.monsterapp.model.state;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.model.manager.state.StateMachine;

/**
 * Stateクラス
 */
@SuppressLint("NewApi")
public abstract class State {
    /** StateCode */
    @NonNull public StateCode stateCode;
    /** StateMachine */
    @NonNull public StateMachine stateMachine;

    /**
     * コンストラクタ
     * @param stateMachine StateMachine
     */
    public State(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        this.stateMachine = stateMachine;
        this.stateCode = stateCode;
    }

    /**
     * 他状態からの遷移時処理
     */
    public void onEnter() {
        Log.d("state enter event", stateCode.toString());
    }

    /**
     * 他状態への遷移時処理
     */
    public void onExit() {
        Log.d("state exit event", stateCode.toString());
    }

    /**
     * 状態遷移処理
     * @param nextStateCode 次の状態コード
     */
    public void onTransition(StateCode nextStateCode) {
        Log.d("state transition", "from " + this.stateCode.toString() + " to " + nextStateCode.toString());
        stateMachine.transition(nextStateCode);
    }

    /**
     * イベント処理関数
     * @param event イベント
     */
    public abstract void handleEvent(@NonNull Event event);

    /**
     * 一時状態 or 永続状態であるかを返す
     * @return 一時状態:true, 永続状態: false
     */
    public abstract boolean isTemporary();
}
