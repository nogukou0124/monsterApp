package com.example.monsterapp.model.manager;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.entity.battle.BattleType;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.manager.battle.BattleManager;
import com.example.monsterapp.model.manager.battle.BattleStatus;
import com.example.monsterapp.model.manager.state.MonsterStateMachine;
import com.example.monsterapp.model.data.repository.MonsterRepository;
import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;
import com.example.monsterapp.util.callback.BattleEventListener;
import com.example.monsterapp.util.callback.MonsterUpdateListener;
import com.example.monsterapp.util.callback.StateEventListener;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Model全体を管理するクラス
 */
public class MonsterManager implements MonsterUpdateListener, StateEventListener,BattleEventListener {
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
        if (monsterStateMachine != null) {
            monsterStateMachine.clear();
            monsterStateMachine = null;
        }
    }

    /**
     * イベント処理
     * @param event イベント
     */
    public void handleEvent(Event event) {
        try {
            Log.d("handle event", String.valueOf(event.eventCode));
            if (monsterStateMachine == null || currentMonster == null) { return; }

            switch (event.eventCode) {
                case ESCAPE:
                    battleManager.cancelNpcBattle();
                    break;
                case WIN:
                case LOSE:
                    currentMonster.hp = currentMonster.maxHp;
                    break;
                case RESET:
                    repository.reset();
                    break;
            }

            monsterStateMachine.handleEvent(event);

        } catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());
        }
    }

    @Override
    public void onUpdatedState(State oldState, State newState) {
        if (currentMonster == null) { return; }
        Log.d("onUpdatedState", newState.stateCode.toString());
        currentMonster.stateCode = newState.stateCode;
        repository.updateMonster(currentMonster);

        if (oldState.stateCode == StateCode.ATTACK) {
            battleManager.executeTurn(false);
        } else if (oldState.stateCode == StateCode.ATTACKED) {
            battleManager.executeTurn(true);
        }
    }

    @Override
    public void onTimeEvent(Event event) {
        handleEvent(event);
    }

    @Override
    public void onUpdatedMonster(@NonNull Monster newMonster) {
        Log.d("onUpdatedState", newMonster.name);
        currentMonster = newMonster;
        // モンスターが初期化されたタイミングでステートマシンを起動する
        if(monsterStateMachine == null) {
            // 初期状態をセットし、状態遷移を開始する
            monsterStateMachine = new MonsterStateMachine(this);
            monsterStateMachine.start(newMonster.stateCode);
        }
        monsterSubject.onNext(newMonster);
    }

    @Override
    public void onChangedBattleStatus(BattleStatus newBattleStatus) {
        Log.d("onChangedBattleStatus", newBattleStatus.toString());
        // 死亡状態の場合は対戦イベントを受け付けない
        if (currentMonster == null || currentMonster.stateCode == StateCode.DEATH) {
            return;
        }


        // 対戦開始のステータスに変化した場合は、対戦開始処理を実行
        // 対戦中のステータスの場合は、イベント処理を実行
        switch (newBattleStatus) {
            case NPC_BATTLE_START:
                battleManager.startBattle(BattleType.NPC, currentMonster);
                break;
            case BLE_BATTLE_START:
                battleManager.startBattle(BattleType.BLE, currentMonster);
                break;
            case ATTACKING:
                handleEvent(new Event(EventCode.ATTACK));
                break;
            case ATTACKED:
                handleEvent(new Event(EventCode.ATTACKED));
                break;
            case WIN:
                handleEvent(new Event(EventCode.WIN));
                break;
            case LOSE:
                handleEvent(new Event(EventCode.LOSE));
                break;
        }

        // 画面側に通知
        battleStatusSubject.onNext(newBattleStatus);
    }
}
