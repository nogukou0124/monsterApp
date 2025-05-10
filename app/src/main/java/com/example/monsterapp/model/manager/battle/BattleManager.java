package com.example.monsterapp.model.manager.battle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.entity.battle.BattleStatus;
import com.example.monsterapp.model.entity.battle.BattleType;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.manager.battle.npc.NPCBattleStrategy;
import com.example.monsterapp.util.battle.BattleUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * 対戦管理クラス
 * 対戦の開始、進行、終了を管理し、状態変化を通知する
 */
public class BattleManager {
    // 状態
    @Nullable private Monster myMonster;
    @Nullable private Monster enemyMonster;
    private boolean isBattling;

    // 対戦戦略
    @Nullable private BattleStrategy battleStrategy;

    // 対戦状態通知
    @NonNull private final BehaviorSubject<BattleStatus> battleStatusSubject = BehaviorSubject.createDefault(BattleStatus.NORMAL);

    // スレッド管理
    @NonNull private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @NonNull private final ExecutorService battleExecutor = Executors.newSingleThreadExecutor();
    @NonNull private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Nullable private Thread npcBattleWaitThread;

    /**
     * コンストラクタ
     */
    public BattleManager() {
        Log.d("BattleManager", "Initializing");
        isBattling = false;
        startNpcBattleScheduler();
    }

    /**
     * 対戦状態を監視するObservableを取得
     * @return 対戦状態を通知するObservable
     */
    @NonNull
    public Observable<BattleStatus> observeBattleStatus() {
        Log.d("BattleManager", "observeBattleStatus current value: " + battleStatusSubject.getValue());
        return battleStatusSubject.hide();
    }

    /**
     * NPC対戦のスケジューラーを起動
     */
    private void startNpcBattleScheduler() {
        Log.d("BattleManager", "Starting NPC battle scheduler");

        scheduler.scheduleWithFixedDelay(() -> {
            if (isBattling) {
                // 対戦中は新たな対戦を開始しない
                Log.d("BattleManager", "Battle already in progress, skipping scheduled NPC battle");
                return;
            }

            Log.d("BattleManager", "Scheduled NPC battle trigger at: " + System.currentTimeMillis());

            // NPC対戦の発生を通知（UIスレッドで）
            mainHandler.post(() -> {
                updateBattleStatus(BattleStatus.READY_NPC_BATTLE);
            });

            // 待機後にNPC対戦を開始
            npcBattleWaitThread = new Thread(() -> {
                try {
                    Thread.sleep(BattleUtils.NPC_BATTLE_TRIGGER_TO_START_TIME);

                    // 対戦開始通知（UIスレッドで）
                    mainHandler.post(() -> {
                        if (!isBattling) { // 二重チェック
                            updateBattleStatus(BattleStatus.NPC_BATTLE_START);
                        }
                    });
                } catch (InterruptedException e) {
                    Log.d("BattleManager", "NPC battle wait interrupted");
                } catch (Exception e) {
                    Log.e("BattleManager", "Error in NPC battle wait thread", e);
                }
            });
            npcBattleWaitThread.start();
        }, 5, BattleUtils.NPC_BATTLE_DURATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * NPC対戦をキャンセル
     */
    public void cancelNpcBattle() {
        Log.d("BattleManager", "Cancelling NPC battle");

        // 待機スレッドの中断
        if (npcBattleWaitThread != null && npcBattleWaitThread.isAlive()) {
            npcBattleWaitThread.interrupt();
            npcBattleWaitThread = null;
        }

        // 通常状態に戻す
        mainHandler.post(() -> {
            updateBattleStatus(BattleStatus.NORMAL);
        });
    }

    /**
     * 対戦を開始
     * @param battleType 対戦タイプ
     * @param myMonster 自分のモンスター
     */
    public void startBattle(@NonNull BattleType battleType, @NonNull Monster myMonster) {

        if (isBattling) {
            Log.w("BattleManager", "Battle already in progress");
            return;
        }

        Log.d("BattleManager", "Starting battle: " + battleType);
        isBattling = true;
        this.myMonster = myMonster.copy();

        // 対戦処理は別スレッドで実行
        battleExecutor.execute(() -> {
            try {
                // 対戦戦略の初期化
                initializeBattleStrategy(battleType);

                if (battleStrategy == null) {
                    Log.e("BattleManager", "Failed to initialize battle strategy");
                    mainHandler.post(() -> endBattle(false));
                    return;
                }

                // 敵モンスターの生成
                enemyMonster = battleStrategy.createEnemyMonster();

                // 先攻/後攻の決定
                boolean isMyTurn = battleStrategy.decideFirstAttacker(this.myMonster, enemyMonster);

                // ターン実行（UIスレッドで通知）
                mainHandler.post(() -> executeTurn(isMyTurn));
            } catch (Exception e) {
                Log.e("BattleManager", "Error starting battle", e);
                mainHandler.post(() -> endBattle(false));
            }
        });
    }

    /**
     * 対戦戦略の初期化
     * @param battleType 対戦タイプ
     */
    private void initializeBattleStrategy(BattleType battleType) {
        switch (battleType) {
            case NPC:
                battleStrategy = new NPCBattleStrategy(this);
                break;
            case BLE:
                // battleStrategy = new BleBattleStrategy(this);
                Log.d("BattleManager", "BLE battle not yet implemented");
                break;
            default:
                Log.w("BattleManager", "Unknown battle type: " + battleType);
                battleStrategy = null;
                break;
        }
    }

    /**
     * ターンを実行
     * @param isMyTurn 自分のターンかどうか
     */
    public void executeTurn(boolean isMyTurn) {
        if (battleStrategy == null || myMonster == null || enemyMonster == null) {
            Log.e("BattleManager", "Cannot execute turn: strategy or monsters not initialized");
            endBattle(false);
            return;
        }

        // 対戦終了チェック
        if (myMonster.hp <= 0) {
            Log.d("BattleManager", "My monster HP is 0, ending battle (lose)");
            endBattle(false);
            return;
        } else if (enemyMonster.hp <= 0) {
            Log.d("BattleManager", "Enemy monster HP is 0, ending battle (win)");
            endBattle(true);
            return;
        }

        // 対戦処理は別スレッドで実行
        battleExecutor.execute(() -> {
            try {
                Log.d("BattleManager", "Executing turn: " + (isMyTurn ? "my turn" : "enemy turn"));

                if (isMyTurn) {
                    battleStrategy.executeMyTurn();

                    // 攻撃状態に更新（UIスレッドで）
                    mainHandler.post(() -> {
                        updateBattleStatus(BattleStatus.ATTACKING);
                    });
                } else {
                    battleStrategy.executeEnemyTurn();

                    // 被攻撃状態に更新（UIスレッドで）
                    mainHandler.post(() -> {
                        updateBattleStatus(BattleStatus.ATTACKED);
                    });
                }
            } catch (Exception e) {
                Log.e("BattleManager", "Error executing turn", e);
                mainHandler.post(() -> endBattle(false));
            }
        });
    }

    /**
     * 対戦を終了
     * @param isWin 勝利かどうか
     */
    public void endBattle(boolean isWin) {
        Log.d("BattleManager", "Ending battle: " + (isWin ? "win" : "lose"));

        // 勝敗状態の通知
        updateBattleStatus(isWin ? BattleStatus.WIN : BattleStatus.LOSE);

        // クリーンアップ処理は別スレッドで実行
        battleExecutor.execute(() -> {
            try {
                if (battleStrategy != null) {
                    battleStrategy.cleanUp();
                    battleStrategy = null;
                }

                // 戦闘終了フラグを設定
                isBattling = false;

                // 通常状態に戻す（UIスレッドで）
                mainHandler.post(() -> {
                    updateBattleStatus(BattleStatus.NORMAL);
                });
            } catch (Exception e) {
                Log.e("BattleManager", "Error cleaning up battle", e);
            }
        });
    }

    /**
     * 対戦状態を更新
     * @param status 新しい対戦状態
     */
    private void updateBattleStatus(BattleStatus status) {
        Log.d("BattleManager", "Updating battle status to: " + status);
        battleStatusSubject.onNext(status);
    }

    /**
     * BLEスキャンを開始
     */
    public void startScan() {
        Log.d("BattleManager", "Starting BLE scan");
        // BLEスキャン実装
    }

    /**
     * モンスターを設定
     * @param myMonster 自分のモンスター
     */
    public void setMyMonster(@Nullable Monster myMonster) {
        this.myMonster = myMonster;
    }

    /**
     * 自分のモンスターを取得
     * @return 自分のモンスター
     */
    @Nullable
    public Monster getMyMonster() {
        return myMonster;
    }

    /**
     * 敵モンスターを取得
     * @return 敵モンスター
     */
    @Nullable
    public Monster getEnemyMonster() {
        return enemyMonster;
    }

    /**
     * 対戦中かどうか
     * @return 対戦中ならtrue
     */
    public boolean isBattling() {
        return isBattling;
    }

    /**
     * リソースのクリーンアップ
     */
    public void cleanup() {
        Log.d("BattleManager", "Cleaning up resources");

        // 対戦中の場合は終了
        if (isBattling && battleStrategy != null) {
            battleStrategy.cleanUp();
            battleStrategy = null;
            isBattling = false;
        }

        // 待機スレッドの中断
        if (npcBattleWaitThread != null && npcBattleWaitThread.isAlive()) {
            npcBattleWaitThread.interrupt();
            npcBattleWaitThread = null;
        }

        // スケジューラのシャットダウン
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        // バトル実行スレッドのシャットダウン
        if (!battleExecutor.isShutdown()) {
            battleExecutor.shutdownNow();
        }
    }
}