package com.example.monsterapp.models.entity.state.temporaryState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.models.entity.state.State;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.models.manager.state.StateMachine;

/**
 * 一時状態
 */
public class TemporaryState extends State {
    public TemporaryState(StateMachine stateMachine, StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        super.handleEvent(event);

        // 一時状態は５秒間状態を維持した後、遷移前の状態に戻る
        if (event.eventCode == EventCode.RETURN) {
            @Nullable State preState = stateMachine.getState(1);
            if (preState == null) {
                onTransition(StateCode.NORMAL);
            }
            else {
                onTransition(preState.stateCode);
            }
        }
    }
}
