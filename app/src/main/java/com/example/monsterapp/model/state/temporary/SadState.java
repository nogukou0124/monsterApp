package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.StateMachine;

/**
 * 悲しみ状態クラス
 */
public class SadState extends TemporaryState {
    public SadState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
