package com.example.monsterapp.model.state.temporary;

import androidx.annotation.NonNull;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.StateMachine;

/**
 * 喜び状態クラス
 */
public class JoyState extends TemporaryState {
    public JoyState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
