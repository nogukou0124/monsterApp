package com.example.monsterapp.usecase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.data.repository.MonsterRepository;
import com.example.monsterapp.model.entity.battle.BattleStatus;
import com.example.monsterapp.model.entity.battle.BattleType;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.manager.battle.BattleManager;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * 対戦機能のユースケースクラス
 * 対戦管理、イベント処理、状態管理を担当
 */
@Singleton
public class BattleUseCase {

    // 依存コンポーネント
    @NonNull private final MonsterRepository repository;
    @NonNull private final BattleManager battleManager;
    @NonNull private final MonsterStateUseCase monsterStateUseCase;

    // 状態
    @Nullable private Monster currentMonster;

    // 通知
    @NonNull private final PublishSubject<BattleStatus> battleStatusSubject = PublishSubject.create();

    // スレッド管理
    @NonNull private final ExecutorService battleExecutor = Executors.newSingleThreadExecutor();
    @NonNull private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * コンストラクタ
     */
    @Inject
    public BattleUseCase(
            @NonNull MonsterRepository repository,
            @NonNull MonsterStateUseCase monsterStateUseCase) {
        this.repository = repository;
        this.monsterStateUseCase = monsterStateUseCase;
        this.battleManager = new BattleManager();

        initSubscriptions();
    }

    /**
     * サブスクリプションの初期化
     */
    private void initSubscriptions() {
        // モンスターの監視
        disposables.add(
                repository.getMonster(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                monster -> {
                                    currentMonster = monster;
                                    if (currentMonster != null) {
                                        battleManager.setMyMonster(currentMonster);
                                    }
                                },
                                error -> Log.e("BattleUseCase", "Error observing monster", error)
                        )
        );

        // 対戦状態の監視
        disposables.add(
                battleManager.observeBattleStatus()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                status -> {
                                    Log.d("BattleUseCase", "Battle status changed: " + status);
                                    // 自身のSubjectに転送
                                    battleStatusSubject.onNext(status);
                                    // 状態に基づく処理
                                    processBattleStatus(status);
                                },
                                error -> Log.e("BattleUseCase", "Error observing battle status", error)
                        )
        );
    }

    /**
     * 対戦状態を監視するObservableを取得
     * @return 対戦状態を通知するObservable
     */
    public Observable<BattleStatus> observeBattleStatus() {
        return battleStatusSubject
                .distinctUntilChanged()
                .doOnSubscribe(d -> Log.d("BattleUseCase", "Battle status subscription started"))
                .doOnNext(status -> Log.d("BattleUseCase", "Emitting battle status: " + status));
    }

    /**
     * 「逃げる」ボタン押下時の処理
     */
    public void onClickEscape() {
        Log.d("BattleUseCase", "Escape clicked");
        battleExecutor.execute(battleManager::cancelNpcBattle);
    }

    /**
     * 通信対戦ボタン押下時の処理
     */
    public void onClickBleBattle() {
        Log.d("BattleUseCase", "BLE battle clicked");
        battleExecutor.execute(battleManager::startScan);
    }

    /**
     * 対戦状態に基づく処理
     * @param status 対戦状態
     */
    private void processBattleStatus(BattleStatus status) {
        if (currentMonster == null) {
            Log.w("BattleUseCase", "Current monster is null, cannot process battle status");
            return;
        }

        Log.d("BattleUseCase", "Processing battle status: " + status);

        switch (status) {
            case NPC_BATTLE_START:
                startBattle(BattleType.NPC);
                break;

            case BLE_BATTLE_START:
                startBattle(BattleType.BLE);
                break;

            case ATTACKING:
                handleAttacking();
                break;

            case ATTACKED:
                handleAttacked();
                break;

            case WIN:
                handleBattleResult(true);
                break;

            case LOSE:
                handleBattleResult(false);
                break;
        }
    }

    /**
     * 対戦開始
     * @param battleType 対戦タイプ
     */
    private void startBattle(BattleType battleType) {
        battleExecutor.execute(() -> {
            try {
                Log.d("BattleUseCase", "Starting " + battleType + " battle");
                battleManager.startBattle(battleType, currentMonster);
            } catch (Exception e) {
                Log.e("BattleUseCase", "Error starting battle", e);
            }
        });
    }

    /**
     * 攻撃時の処理
     */
    private void handleAttacking() {
        Log.d("BattleUseCase", "Handling attacking");
        monsterStateUseCase.handleEvent(new Event(EventCode.ATTACK));
    }

    /**
     * 攻撃を受けた時の処理
     */
    private void handleAttacked() {
        Log.d("BattleUseCase", "Handling attacked");

        // モンスターのHP更新
        disposables.add(
                Observable.fromCallable(() -> {
                            if (currentMonster == null) { throw new NullPointerException("モンスターが正しくセットされていません"); }
                            if (battleManager.getMyMonster() != null) {
                                currentMonster.hp = battleManager.getMyMonster().hp;
                                repository.updateMonster(currentMonster);
                            }
                            return true;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> monsterStateUseCase.handleEvent(new Event(EventCode.ATTACKED)),
                                error -> Log.e("BattleUseCase", "Error updating monster HP", error)
                        )
        );
    }

    /**
     * 対戦結果の処理
     * @param isWin 勝利かどうか
     */
    private void handleBattleResult(boolean isWin) {
        Log.d("BattleUseCase", "Handling battle result: " + (isWin ? "win" : "lose"));

        // 状態更新
        monsterStateUseCase.handleEvent(new Event(isWin ? EventCode.WIN : EventCode.LOSE));

        // HP回復処理
        disposables.add(
                Observable.fromCallable(() -> {
                            if (currentMonster != null) {
                                currentMonster.hp = currentMonster.maxHp;
                                repository.updateMonster(currentMonster);
                            }
                            return true;
                        })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                result -> Log.d("BattleUseCase", "Monster HP restored after battle"),
                                error -> Log.e("BattleUseCase", "Error restoring monster HP", error)
                        )
        );
    }

    /**
     * リソースのクリーンアップ
     */
    public void cleanup() {
        Log.d("BattleUseCase", "Cleaning up resources");

        // サブスクリプション解除
        disposables.clear();

        // バトルマネージャーのクリーンアップ
        battleManager.cleanup();

        // スレッドプールのシャットダウン
        if (!battleExecutor.isShutdown()) {
            battleExecutor.shutdown();
        }
    }
}