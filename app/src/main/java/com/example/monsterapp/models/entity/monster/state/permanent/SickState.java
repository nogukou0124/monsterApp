package com.example.monsterapp.models.entity.monster.state.permanent;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.models.manager.state.StateMachine;
import com.example.monsterapp.utils.state.StateUtils;

/**
 * 病気状態クラス
 */
public class SickState extends State {
    /** 病気状態になった時間 */
    private long lastSickTime = 0;

    public SickState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
    }

    @SuppressLint("NewApi")
    @Override
    public void onEnter() {
        super.onEnter();
        lastSickTime = System.currentTimeMillis();
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        // 活動時間外なら睡眠状態に遷移する
        if (StateUtils.isSleepTime()) {
            onTransition(StateCode.SLEEP);
            return;
        }

        switch (event.eventCode) {
            case CURE:
                onTransition(StateCode.NORMAL);
                break;
            case TIME:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSickTime > StateUtils.SICK_TO_DEATH_TIME) {
                    onTransition(StateCode.DEATH);
                }
                break;
            default:
                // その他のイベントハンドリングは共通ロジックを利用する
                @Nullable StateCode stateCode = StateUtils.handleCommonEvent(event);
                if (stateCode != null) {
                    onTransition(stateCode);
                    break;
                }
        }
    }

    @Override
    public boolean isTemporary() {
        return false;
    }
}
