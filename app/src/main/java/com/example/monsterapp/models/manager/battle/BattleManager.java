package com.example.monsterapp.models.manager.battle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.battle.BattleData;
import com.example.monsterapp.models.entity.battle.BattleStatus;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.manager.MonsterManager;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * 対戦管理クラス
 */
public class BattleManager {
    /** Manager */
    @NonNull private MonsterManager monsterManager;
    /** Strategy */
    @Nullable private BattleStrategy battleStrategy;

    /** 自分のモンスター */
    @Nullable private Monster selfMonster;
    /** 対戦前の自分のモンスター */
    @Nullable private Monster initSelfMonster;
    /** 相手のモンスター　*/
    @Nullable private Monster enemyMonster;

    /**
     * コンストラクタ
     */
    public BattleManager(@NonNull MonsterManager monsterManager) {
        this.monsterManager = monsterManager;

        // 12時間に一度、NPC対戦を始める
        final int ONE_SECOND = 1000;
        final int SECOND_PER_MINUTE = 60;
        final int ONE_HOUR = ONE_SECOND * SECOND_PER_MINUTE * 60;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("TimerTask", "Task executed at: " + System.currentTimeMillis());
                monsterManager.handleEvent(new Event(EventCode.NPC_BATTLE));

            }
        }, 0, 12 * ONE_HOUR);
    }

    /**
     * プレイヤーモンスターの
     * @param selfMonster 自分のモンスター
     */
    public void setPlayerMonster(@NonNull Monster selfMonster) {
        this.selfMonster = selfMonster;
        // 対戦前のモンスター情報を残しておく（対戦後、元の情報に書き換えるため）
        initSelfMonster = new Monster(
                selfMonster.uid,
                selfMonster.stateCode,
                selfMonster.name,
                selfMonster.hp,
                selfMonster.power
        );
    }

    /**
     * モンスターの状態を元にダメージを計算する
     * @param attacker 攻撃者
     * @param defender 被攻撃者
     */
    public int getDamage(Monster attacker, Monster defender) {
        Random random = new Random();
        // 命中率は基本80％
        // ただし、攻撃者が病気の場合は20%低下、被攻撃者が病気の場合15%増加
        double hitRate = 0.8;
        hitRate = attacker.stateCode == StateCode.SICK ? hitRate - 0.2 : hitRate;
        hitRate = defender.stateCode == StateCode.SICK ? hitRate + 0.15 : hitRate;

        // 命中した場合は、攻撃者の攻撃力分ダメージを与える。
        // 攻撃を外した場合は、0ダメージ。
        if (random.nextDouble() <= hitRate) {
            return attacker.power;
        }
        else {
            return 0;
        }
    }
}
