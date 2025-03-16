package com.example.monsterapp.model.state.permanent;


import static com.example.monsterapp.util.Event.EventCode.*;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.model.manager.state.StateMachine;
import com.example.monsterapp.util.state.StateUtils;

/**
 * 通常状態クラス
 */
@SuppressLint("NewApi")
public class NormalState extends State {
    /**
     * 最後にエサを食べた時間
     */
    private long lastMealTime = 0;
    /**
     * 最後に糞をした時間
     */
    private long lastPoopedTime = 0;
    /**
     * 糞判定フラグ
     */
    private boolean isPooped = false;

    /**
     * コンストラクタ
     */
    public NormalState(@NonNull StateMachine stateMachine, @NonNull StateCode stateCode) {
        super(stateMachine, stateCode);
        lastMealTime = System.currentTimeMillis();
    }

    @Override
    public void handleEvent(@NonNull Event event) {
        // 活動時間外なら睡眠状態に遷移する
        if (StateUtils.isSleepTime()) {
            onTransition(StateCode.SLEEP);
            return;
        }

        long currentTime = System.currentTimeMillis();
        switch (event.eventCode) {
            case FEED:
                // 空腹状態ならエサをあげると喜ぶ
                // 満腹状態にエサをあげると拒否する
                if (currentTime - lastMealTime >= StateUtils.FULL_TO_HUNGRY_TIME) {
                    onTransition(StateCode.JOY);
                    lastMealTime = System.currentTimeMillis();
                } else {
                    onTransition(StateCode.DENY);
                }
                break;
            case TOILET:
                /// 糞をしている時トイレに行くと喜ぶ
                // 糞をしていない時はトイレを拒否する
                if (isPooped) {
                    onTransition(StateCode.JOY);
                    isPooped = false;
                } else {
                    onTransition(StateCode.DENY);
                }
                break;

            case TIME:
                // 満腹状態で3時間経過すると空腹状態になる
                // 空腹状態が2時間続くと死亡する
                if (currentTime - lastMealTime >= StateUtils.FULL_TO_HUNGRY_TIME + StateUtils.HUNGRY_TO_DEATH_TIME) {
                    onTransition(StateCode.DEATH);
                    return;
                }

                // 満腹状態になってから15分後に糞をする
                if (!isPooped && (currentTime - lastMealTime > StateUtils.MEAL_TO_POOP_TIME)) {
                    lastPoopedTime = System.currentTimeMillis();
                    isPooped = true;
                    return;
                }
                // 糞をしてから4時間経過すると病気になる
                if (isPooped && (currentTime - lastPoopedTime > StateUtils.POOP_TO_SICK_TIME)) {
                    onTransition(StateCode.SICK);
                    return;
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
    public boolean isTemporary () {
        return false;
    }
}
