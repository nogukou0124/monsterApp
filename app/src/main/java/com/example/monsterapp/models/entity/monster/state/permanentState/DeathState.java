package com.example.monsterapp.models.entity.monster.state.permanentState;

import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.manager.state.StateMachine;

/**
 * 死亡状態クラス
 */
public class DeathState extends State {
    public DeathState(StateMachine stateMachine, StateCode stateCode) {
        super(stateMachine, stateCode);
    }
}
