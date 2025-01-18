package com.example.monsterapp.models.entity.state.permanentState;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.models.entity.state.State;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.utils.TimeConstants;
import com.example.monsterapp.models.manager.state.StateMachine;

import java.time.LocalTime;

/**
 * 睡眠状態
 */
public class SleepState extends State {

    public SleepState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @SuppressLint("NewApi")
    @Override
    public void handleEvent(@NonNull Event event) {
        if (event.eventCode != EventCode.TIME) { return; }

        LocalTime now = LocalTime.now();
        // 活動時間内になったら活動状態に遷移
        // 元の活動状態に遷移する
        if (now.isAfter(TimeConstants.ACTIVITY_START_TIME) && now.isBefore(TimeConstants.ACTIVITY_END_TIME)) {
            @Nullable State nextState = stateMachine.getState(1);
            if (nextState == null) {
                onTransition(StateCode.NORMAL);
            }
            else {
                onTransition(nextState.stateCode);
            }
        }
    }
}
