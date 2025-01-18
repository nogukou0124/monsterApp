package com.example.monsterapp.models.entity.state.permanentState;

import androidx.annotation.NonNull;

import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.models.entity.state.State;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.models.manager.state.StateMachine;

/**
 * 死亡状態クラス
 */
public class DeathState extends State {
    public DeathState(StateMachine stateMachine, StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
