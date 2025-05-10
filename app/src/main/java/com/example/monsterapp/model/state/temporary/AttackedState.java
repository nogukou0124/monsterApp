package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.StateMachine;

/**
 * 被攻撃状態クラス
 */
public class AttackedState extends TemporaryState {
    public AttackedState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
