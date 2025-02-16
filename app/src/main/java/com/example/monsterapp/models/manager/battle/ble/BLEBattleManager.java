package com.example.monsterapp.models.manager.battle.ble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.battle.BattleStatus;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.manager.battle.BattleManager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.rxjava3.core.Observable;

public class BLEBattleManager extends BattleManager {
    @NonNull private final BleScanner scanner = new BleScanner();
    @NonNull private final BleAdvertiser advertiser = new BleAdvertiser();

    /**
     * コンストラクタ
     * @param playerMonster 自分のモンスター
     */
    public BLEBattleManager(Monster playerMonster) {
        super(playerMonster);
    }

    public void startConnect() {
        battleSubject.onNext(BattleStatus.CONNECTING);
        return scanner.startScan()
                .mergeWith(advertiser.startAdvertising())
                .timeout(10, TimeUnit.SECONDS)
                .map(success -> )
    }

    @Override
    public void startBattle() {
    }
}
