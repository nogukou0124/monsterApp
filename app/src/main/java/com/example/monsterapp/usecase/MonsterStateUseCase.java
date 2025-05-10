package com.example.monsterapp.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.data.repository.MonsterRepository;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.state.StateMachine;
import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.state.permanent.DeathState;
import com.example.monsterapp.model.state.permanent.NormalState;
import com.example.monsterapp.model.state.permanent.SickState;
import com.example.monsterapp.model.state.permanent.SleepState;
import com.example.monsterapp.model.state.temporary.AttackState;
import com.example.monsterapp.model.state.temporary.AttackedState;
import com.example.monsterapp.model.state.temporary.DenyState;
import com.example.monsterapp.model.state.temporary.JoyState;
import com.example.monsterapp.model.state.temporary.MealState;
import com.example.monsterapp.model.state.temporary.SadState;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * モンスターの状態管理に関するユースケースクラス
 */
@Singleton
public class MonsterStateUseCase {
    @NonNull private final MonsterRepository repository;
    @NonNull private final StateMachine stateMachine;

    @NonNull private final CompositeDisposable disposable = new CompositeDisposable();

    @Nullable private Monster currentMonster;

    /**
     * コンストラクタ
     * @param repository モンスターリポジトリ
     */
    @Inject
    public MonsterStateUseCase(@NonNull MonsterRepository repository) {
        this.repository = repository;
        this.stateMachine = new StateMachine();

        // state
        stateMachine.addState(new NormalState(stateMachine, StateCode.NORMAL));
        stateMachine.addState(new SickState(stateMachine, StateCode.SICK));
        stateMachine.addState(new DeathState(stateMachine, StateCode.DEATH));
        stateMachine.addState(new SleepState(stateMachine, StateCode.SLEEP));
        stateMachine.addState(new JoyState(stateMachine, StateCode.JOY));
        stateMachine.addState(new SadState(stateMachine, StateCode.SAD));
        stateMachine.addState(new DenyState(stateMachine, StateCode.DENY));
        stateMachine.addState(new MealState(stateMachine, StateCode.MEAL));
        stateMachine.addState(new AttackState(stateMachine, StateCode.ATTACK));
        stateMachine.addState(new AttackedState(stateMachine, StateCode.ATTACKED));

        // モンスターデータの監視を開始
        disposable.add(
            repository.getMonster(1)
                    .subscribe(
                            monster -> {
                                currentMonster = monster;

                                // 初期状態の設定（最初のモンスター取得時のみ）
                                if (stateMachine.getCurrentState() == null) {
                                    stateMachine.setCurrentState(monster.stateCode);
                                    stateMachine.setTimeEvent();
                                }
                            },
                            error -> Log.e("MonsterStateUseCase", "Error observing monster", error)
                    )
        );
    }

    /**
     * 現在の状態を監視するためのObservableを取得
     * @return 状態を発行するObservable
     */
    @NonNull
    public BehaviorSubject<State> observeCurrentState() {
        return stateMachine.observeCurrentState();
    }

    /**
     * イベント処理
     * @param event イベント
     */
    public void handleEvent(@NonNull Event event) {
        if (currentMonster == null) return;
        if (event.eventCode == EventCode.RESET) {
            repository.reset();
        }
        stateMachine.handleEvent(event);
    }

    /**
     * リソースのクリーンアップ
     */
    public void cleanup() {
        stateMachine.clear();
        disposable.clear();
    }
}