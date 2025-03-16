package com.example.monsterapp.model.manager.state;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.Event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * StateMachineクラス
 */
public class StateMachine {
    /** StateMap */
    private @NonNull Map<StateCode, State> stateMap = new HashMap<>();
    /** 現在のState */
    private @Nullable State currentState;
    /** １つ前の永続State　*/
    private @Nullable State preState;

    /**
     * コンストラクタ
     */
    public StateMachine() {}

    /**
     * 状態の追加
     * @param state　追加する状態
     */
    public void addState(@NonNull State state) {
        this.stateMap.put(state.stateCode, state);
        state.stateMachine = this;
    }

    /**
     * 現在状態のSetter
     * @param stateCode　状態コード
     */
    public void setCurrentState(@NonNull StateCode stateCode) {
        currentState = stateMap.get(stateCode);
        if (currentState == null) {
            throw new NullPointerException("存在しない状態に遷移できません");
        }
    }

    /**
     * 現在状態のgetter
     * @return 現在の状態
     */
    @Nullable public State getCurrentState() { return currentState; }

    /**
     * 1つ前状態のgetter
     * @return 1つ前の状態
     */
    @Nullable public State getPreState() { return preState; }

    /**
     * 状態遷移関数
     * @param nextStateCode 遷移後の状態
     */
    public void transition(@NonNull StateCode nextStateCode) {
        if (currentState != null) {
            preState = currentState;
            currentState.onExit();
        }

        currentState = stateMap.get(nextStateCode);
        if (currentState == null) {
            return;
        }
        currentState.onEnter();
    }

    /**
     * クリア関数
     */
    public void clear() {
        if (currentState != null) {
            currentState.onExit();
        }
        currentState = null;
        stateMap = new HashMap<>();
    }

    /**
     * イベント関数
     */
    public void handleEvent(@NonNull Event event) {
        if (currentState != null) {
            Log.d("state handle event", "current state is " + currentState.stateCode.toString());
            currentState.handleEvent(event);
        }
    }
}
