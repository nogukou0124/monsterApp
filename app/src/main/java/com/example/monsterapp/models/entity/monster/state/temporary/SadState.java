package com.example.monsterapp.models.entity.monster.state.temporary;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.manager.state.StateMachine;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.state.StateUtils;

/**
 * 悲しみ状態クラス
 */
public class SadState extends State {
    public SadState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @Override
    public void handleEvent(@NonNull Event event) {

    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
