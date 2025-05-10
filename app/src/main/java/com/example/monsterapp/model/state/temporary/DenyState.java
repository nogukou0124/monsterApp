package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.StateMachine;

/**
 * 拒否状態クラス
 */
public class DenyState extends TemporaryState {
    public DenyState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
