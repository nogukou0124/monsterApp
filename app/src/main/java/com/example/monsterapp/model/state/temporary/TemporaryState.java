package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.StateMachine;
import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;
import com.example.monsterapp.util.state.StateUtils;

/**
 * 一時状態クラス
 */
public class TemporaryState extends State {
    /**
     * コンストラクタ
     * @param stateMachine StateMachine
     * @param stateCode StateCode
     */
    public TemporaryState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        // 一時状態の場合は5秒間その状態を維持したあと、前の通常状態に遷移する
        new Thread(() -> {
            try {
                Thread.sleep(StateUtils.TEMPORARY_DURATION_TIME);
                State newState = stateMachine.getPreState();
                if (newState == null) {
                    onTransition(StateCode.NORMAL);
                } else {
                    onTransition(newState.stateCode);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        if (event.eventCode == EventCode.RESET) {
            onTransition(StateCode.NORMAL);
        }
    }

    @Override
    public boolean isTemporary() {
        return false;
    }
}
