package com.example.monsterapp.models.manager.battle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.battle.BattleData;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;

import java.util.Random;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * 対戦管理クラス
 */
public abstract class BattleManager {
    /** 自分のモンスター */
    @NonNull protected Monster playerMonster;
    /** 対戦前の自分のモンスター */
    public Monster preBattleMonster;
    /** 相手のモンスター　*/
    @Nullable protected Monster enemyMonster;
    /** subject */
    @NonNull protected final BehaviorSubject<BattleData> battleDataBehaviorSubject = BehaviorSubject.create();

    /**
     * コンストラクタ
     * @param playerMonster 自分のモンスター
     */
    public BattleManager(Monster playerMonster) {
        this.playerMonster = playerMonster;
        preBattleMonster = new Monster(
                playerMonster.uid,
                playerMonster.stateCode,
                playerMonster.name,
                playerMonster.hp,
                playerMonster.power
        );
    }

    /**
     * behaviorSubjectのgetter
     * @return モンスターのbehaviorSubject
     */
    @NonNull
    public BehaviorSubject<BattleData> getMonsterBehaviorSubject() {
        return battleDataBehaviorSubject;
    }

    /**
     * 対戦処理
     */
    public abstract void startBattle();

    /**
     * 攻撃処理
     * @param attacker 攻撃者
     * @param defender 被攻撃者
     */
    public void attack(Monster attacker, Monster defender) {
        if (attacker == playerMonster) {
            onTurnChanged(new Event(EventCode.ATTACK));
        }
        else {
            onTurnChanged(new Event(EventCode.ATTACKED));
        }

        Random random = new Random();
        // 命中率を設定
        // 命中率はモンスターの状態に依存している
        double hitRate = 0.8;
        hitRate = attacker.stateCode == StateCode.SICK ? hitRate - 0.2 : hitRate;
        hitRate = defender.stateCode == StateCode.SICK ? hitRate + 0.15 : hitRate;

        if (random.nextDouble() <= hitRate) {
            defender.hp -= attacker.power;
            if (defender.hp < 0) {
                defender.hp = 0;
            }
        }
        Log.d("turn change", String.valueOf(playerMonster.hp));
    }

    /**
     * コールバック処理（攻守交代時）
     * @param event イベント
     */
    public void onTurnChanged(Event event) {
        battleDataBehaviorSubject.onNext(new BattleData(playerMonster, event));
    }

    /**
     * コールバック処理（対戦終了時）
     * @param event イベント
     */
    public void onFinished(Event event) {
        battleDataBehaviorSubject.onNext(new BattleData(preBattleMonster, event));
    }
}
