package com.example.monsterapp.models.manager;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.monster.state.State;
import com.example.monsterapp.models.manager.battle.BattleManager;
import com.example.monsterapp.models.manager.state.MonsterStateMachine;
import com.example.monsterapp.models.repository.MonsterRepository;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Model全体を管理するクラス
 */
public class MonsterManager {
    /** Roomデータベース操作を行う */
    @Nullable private MonsterRepository repository = null;
    /** 状態遷移を管理 */
    @Nullable private MonsterStateMachine monsterStateMachine = null;
    /** 対戦機能を管理　*/
    @Nullable private BattleManager battleManager = null;
    /** Subject */
    @NonNull private final BehaviorSubject<Monster> monsterSubject = BehaviorSubject.create();

    public MonsterManager(Application application) {
        // モンスター情報が更新された際に通知を受け取る
        repository = MonsterRepository.getInstance(application);
        @NonNull Monster monster = repository.getMonster();

        // 初期状態をセットし、状態遷移を監視する
        monsterStateMachine = new MonsterStateMachine(this);
        monsterStateMachine.start(monster.stateCode);

        battleManager = new BattleManager(this);

    }

    @NonNull
    public BehaviorSubject<Monster> getMonsterSubject() { return monsterSubject; }

    public void clear() {
        repository = null;
        monsterStateMachine = null;
        battleManager = null;
    }

    /**
     * イベント処理
     * @param event イベント
     */
    public void handleEvent(Event event) {
        Log.d("handle event", String.valueOf(event.eventCode));
        if (monsterStateMachine == null) { return; }

        // 通信対戦イベントの場合、通信を開始する
        if (event.eventCode == EventCode.BLE_BATTLE) {

        }
        else {
            monsterStateMachine.handleEvent(event);
        }
    }

    /**
     * モンスターの状態を更新する
     * @param newState 新しい状態
     */
    public void updateState(@NonNull State newState) {
        if (repository == null) { return; }
        Monster newMonster = repository.getMonster();

        // モンスターの状態を更新しViewModelに通知する
        newMonster.stateCode = newState.stateCode;
        repository.updateMonster(newMonster);
        monsterSubject.onNext(newMonster);
    }


}
