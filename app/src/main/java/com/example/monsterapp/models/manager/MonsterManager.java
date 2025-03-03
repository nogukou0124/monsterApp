package com.example.monsterapp.models.manager;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.battle.BattleStatus;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.manager.battle.BattleManager;
import com.example.monsterapp.models.manager.state.MonsterStateMachine;
import com.example.monsterapp.models.repository.MonsterRepository;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Model全体を管理するクラス
 */
public class MonsterManager {
    // State
    /** モンスター */
    @Nullable private Monster currentMonster;

    // Object
    /** Roomデータベース操作を行う */
    @NonNull private final MonsterRepository repository;
    /** 状態遷移を管理 */
    @Nullable private MonsterStateMachine monsterStateMachine = null;
    /** 対戦機能を管理　*/
    @NonNull private BattleManager battleManager;
    /** Subject(モンスター) */
    @NonNull public final BehaviorSubject<Monster> monsterSubject = BehaviorSubject.create();
    /** Subject(対戦管理) */
    @NonNull public final BehaviorSubject<BattleStatus> battleStatusSubject = BehaviorSubject.create();

    /**
     * コンストラクタ
     * @param application application
     */
    public MonsterManager(Application application) {
        // モンスター情報が更新された際に通知を受け取る
        repository = MonsterRepository.getInstance(application, this);
        battleManager = new BattleManager(this);
    }

    public void clear() {
        monsterStateMachine = null;
    }

    /**
     * イベント処理
     * @param event イベント
     */
    public void handleEvent(Event event) {
        try {
            Log.d("handle event", String.valueOf(event.eventCode));
            if (monsterStateMachine == null) { return; }

            EventCode eventCode = event.eventCode;

            if (eventCode == EventCode.ESCAPE) {
                // NPC対戦を中止する
                battleManager.cancelNpcBattle();
                return;
            }
            // 対戦系のイベントは

            if(eventCode == EventCode.BLE_BATTLE) {
                Log.d("BLE Battle Event", "start connect");
            }
            else {
                monsterStateMachine.handleEvent(event);
            }
        } catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());
        }
    }

    public void onChangedMonster(@NonNull Monster newMonster) {
        currentMonster = newMonster;
        // モンスターが初期化されたタイミングでステートマシンを起動する
        if(monsterStateMachine == null) {
            // 初期状態をセットし、状態遷移を開始する
            monsterStateMachine = new MonsterStateMachine(this);
            monsterStateMachine.start(newMonster.stateCode);
        }
        Log.d("current state manager", newMonster.stateCode.toString());
        monsterSubject.onNext(newMonster);
    }

    /**
     * モンスターの状態を更新する
     * @param newMonster 新しいモンスター
     */
    public void updateMonster(@NonNull Monster newMonster) {
        repository.updateMonster(newMonster);
    }

    /**
     * 対戦状態を設定し、ViewModelに通知する
     * @param newBattleStatus 新しい対戦状態
     */
    public void postBattleStatus(BattleStatus newBattleStatus) {
        Log.d("battle Status change", newBattleStatus.toString());
        battleStatusSubject.onNext(newBattleStatus);
    }

    /**
     * モンスター情報のgetter
     * @return 現在のモンスター
     */
    @Nullable public Monster getCurrentMonster() { return currentMonster; }
}
