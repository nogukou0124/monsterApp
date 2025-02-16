package com.example.monsterapp.models.entity.monster.state.permanentState;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.models.manager.state.StateMachine;

import java.time.LocalTime;

/**
 * 病気状態クラス
 */
public class SickState extends State {
    /** 病気状態になった時間 */
    private LocalTime lastSickTime;

    public SickState(StateMachine stateMachine, StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @SuppressLint("NewApi")
    @Override
    public void onEnter() {
        super.onEnter();
        lastSickTime = LocalTime.now();
    }

    @SuppressLint("NewApi")
    @Override
    public void handleEvent(@NonNull Event event) {
        super.handleEvent(event);
        if (event.eventCode == EventCode.CURE) {
            onTransition(StateCode.NORMAL);
            return;
        }

        if (event.eventCode == EventCode.TIME) {
            // 病気状態が2時間続くと死亡する
            LocalTime now = LocalTime.now();
            if (now.isAfter(lastSickTime.plusHours(1))) {
                onTransition(StateCode.DEATH);
            }
        }
    }
}
