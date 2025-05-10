package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.StateMachine;

/**
 * 攻撃状態クラス
 */
public class AttackState extends TemporaryState {
    public AttackState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
