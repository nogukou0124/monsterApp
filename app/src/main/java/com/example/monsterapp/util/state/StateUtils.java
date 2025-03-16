package com.example.monsterapp.util.state;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.Event.Event;

import java.time.LocalTime;

/**
 * StateのUtilクラス
 */
@SuppressLint("NewApi")
public class StateUtils {
    /** 活動開始時間 */
    public static final LocalTime ACTIVITY_START_TIME = LocalTime.of(7,0);
    /** 活動終了時間　*/
    public static final LocalTime ACTIVITY_END_TIME = LocalTime.of(20,0);
    /** 一時状態の持続時間 **/
    public static final long TEMPORARY_DURATION_TIME = 5 * 1000;
    /** 時間遷移タスクの実行間隔 */
    public static final long TIME_EVENT_DURATION_TIME = 15 * 60 * 1000;
    /** 満腹→空腹までの時間 */
    public static final long FULL_TO_HUNGRY_TIME = 3 * 60 * 60 * 1000;
    /** 空腹→死亡までの時間 */
    public static final long HUNGRY_TO_DEATH_TIME = 2 * 60 * 60 * 1000;
    /** 食事→糞をするまでの時間 */
    public static final long MEAL_TO_POOP_TIME = 15 * 60 * 1000;
    /** 糞をしてから病気になるまでの時間　*/
    public static final long POOP_TO_SICK_TIME = 4 * 60 * 60 * 1000;
    /** 病気→死亡までの時間 */
    public static final long SICK_TO_DEATH_TIME = 2 * 60 * 60 * 1000;

    /**
     * stateの共通イベントを定義する
     * @param event イベント
     * @return 状態遷移する場合:次のStateCode, 遷移しない場合:Null
     */
    @Nullable public static StateCode handleCommonEvent(Event event) {
        Log.d("handle state common event", event.eventCode.toString());
        switch (event.eventCode) {
            case RESET:
                return StateCode.NORMAL;
            case ATTACK:
                return StateCode.ATTACK;
            case ATTACKED:
                return StateCode.ATTACKED;
            case WIN:
                return StateCode.JOY;
            case LOSE:
                return StateCode.SAD;
            default:
                return null;
        }
    }

    /**
     * モンスターが睡眠状態か判定する
     * @return 睡眠状態:true 活動状態:false
     */
    public static boolean isSleepTime() {
        LocalTime now = LocalTime.now();
        return now.isBefore(ACTIVITY_START_TIME) || now.isAfter(ACTIVITY_END_TIME);
    }
}
