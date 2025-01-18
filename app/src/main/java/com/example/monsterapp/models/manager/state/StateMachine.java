package com.example.monsterapp.models.manager.state;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.models.entity.state.State;
import com.example.monsterapp.models.entity.state.StateCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StateMachineクラス
 */
public class StateMachine {
    /** StateMap */
    private @NonNull Map<StateCode, State> stateMap = new HashMap<>();
    /** 現在のState */
    private @Nullable State currentState;
    /** State履歴　*/
    private @NonNull List<StateCode> stateHistory = new ArrayList<>();

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
     * 状態のSetter
     * @param stateCode　状態コード
     */
    public void setCurrentState(@NonNull StateCode stateCode) {
        currentState = stateMap.get(stateCode);
        if (currentState != null) {
            stateHistory.add(currentState.stateCode);
            currentState.onEnter();
        }
    }

    /**
     * 状態遷移の履歴から指定位置の状態を取得する
     * @param index 指定位置(カレントは0)
     * @return 指定した状態
     */
    @Nullable
    public State getState(int index) {
        if (stateHistory.isEmpty()) { return null; }
        StateCode stateCode =  stateHistory.get((stateHistory.size() - 1) - index);
        return stateMap.get(stateCode);
    }

    /**
     * 状態遷移関数
     * @param nextStateCode 遷移後の状態
     */
    public void transition(@NonNull StateCode nextStateCode) {
        if (currentState != null) {
            currentState.onExit();
        }

        setCurrentState(nextStateCode);
    }

    /**
     * クリア関数
     */
    public void clear() {
        if (currentState != null) {
            currentState.onExit();
        }
        currentState = null;
        stateHistory = new ArrayList<>();
        stateMap = new HashMap<>();
    }

    /**
     * イベント関数
     */
    public void handleEvent(@NonNull Event event) {
        Log.d("handle event", event.eventCode.toString());
        if (currentState != null) {
            currentState.handleEvent(event);
        }
    }
}
