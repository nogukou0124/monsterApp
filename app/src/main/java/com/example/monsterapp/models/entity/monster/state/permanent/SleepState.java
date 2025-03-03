package com.example.monsterapp.models.entity.monster.state.permanent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.models.manager.state.StateMachine;
import com.example.monsterapp.utils.state.StateUtils;

/**
 * 睡眠状態
 */
public class SleepState extends State {
    public SleepState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        // 活動時間内になったら活動状態に遷移
        // 元の活動状態に遷移する
        if (!StateUtils.isSleepTime()) {
            @Nullable State nextState = stateMachine.getPreState();
            if (nextState == null) {
                onTransition(StateCode.NORMAL);
            }
            else {
                onTransition(nextState.stateCode);
            }
        }
    }

    @Override
    public boolean isTemporary() {
        return false;
    }
}
