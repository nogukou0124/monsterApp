package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.manager.state.StateMachine;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;

/**
 * 攻撃状態クラス
 */
public class AttackState extends State {
    public AttackState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        if (event.eventCode == EventCode.RESET) {
            onTransition(StateCode.NORMAL);
        }
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
