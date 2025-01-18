package com.example.monsterapp.models.entity.state.permanentState;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.models.entity.state.State;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.models.manager.state.StateMachine;

import java.time.LocalTime;

/**
 * 通常状態クラス
 */
@SuppressLint("NewApi")
public class NormalState extends State {

    /** 最後にエサを食べた時間 */
    @NonNull private LocalTime lastFedTime = LocalTime.now();

    /** 最後に糞をした時間 */
    @Nullable private LocalTime lastShitTime;

    /**
     * コンストラクタ
     */
    public NormalState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        // 満腹状態からスタート
        lastFedTime = LocalTime.now();
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        super.handleEvent(event);
        LocalTime now = LocalTime.now();
        switch (event.eventCode) {
            case FEED:
                // 空腹状態の場合は、エサをもらう
                if (now.isAfter(lastFedTime.plusHours(3))) {
                    lastFedTime = LocalTime.now();
                    onTransition(StateCode.JOY);
                }
                // 満腹状態の場合は、エサを拒否する
                else {
                    onTransition(StateCode.DENY);
                }
                break;
            case TOILET:
                // 糞を処理する
                lastShitTime = null;
                break;

            case TIME:

                // 満腹状態で3時間経過すると空腹状態になる
                // 空腹状態が2時間続くと死亡する
                if (now.isAfter(lastFedTime.plusHours(5))) {
                    this.stateMachine.transition(StateCode.DEATH);
                }
                // 糞をしていない場合、もしくは処理されている場合、lastShitTimeはnull
                if (lastShitTime == null) {
                    // エサを食べてから15分経過すると糞をする
                    if(now.isAfter(lastFedTime.plusMinutes(15))) {
                        lastShitTime = now;
                    }
                }
                else {
                    // 糞をしてから4時間経過すると病気になる
                    if (now.isAfter(lastShitTime.plusHours(4))) {
                        this.stateMachine.transition(StateCode.SICK);
                    }
                }
                break;
            default:
                break;
        }
    }
}
