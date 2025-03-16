package com.example.monsterapp.model.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.manager.MonsterManager;
import com.example.monsterapp.model.data.room.AppDatabase;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.data.dao.MonsterDao;
import com.example.monsterapp.util.callback.MonsterUpdateListener;

import java.util.Objects;


/**
 * モンスター情報を管理するクラス
 * ViewModelの橋渡しを行う
 */
public class MonsterRepository {
    /** Singletonインスタンス */
    @Nullable private static MonsterRepository INSTANCE = null;
    /** DAO */
    @Nullable MonsterDao monsterDao = null;
    @NonNull MonsterUpdateListener monsterUpdateListener;

    /**
     * コンストラクタ
     * @param application　コンテキスト
     */
    private MonsterRepository(@NonNull Application application, @NonNull MonsterUpdateListener monsterUpdateListener) {
        this.monsterUpdateListener = monsterUpdateListener;
        try {
            // データベース接続
            @NonNull AppDatabase db = AppDatabase.getDatabase(application);
            monsterDao = db.monsterDao();

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Monster monster = Objects.requireNonNull(monsterDao,"monsterDao is null").getByUid(1);
                // データベースに情報が登録されていない場合は、デフォルトデータを挿入
                if (monster == null) {
                    Log.e("database event", "No Monster found with uid = 1");
                    monster = new Monster(1, StateCode.NORMAL, "アグモン", 6, 6,  3);
                    monsterDao.insert(monster);
                }
                // モンスターの初期化に成功したらManagerに通知
                monsterUpdateListener.onUpdatedMonster(monster);
            });
        } catch (Exception e) {
            Log.e("database event", "cannot get monster", e);
        }
    }

    /**
     * Singletonインスタンスを返す
     * @param application　コンテキスト
     * @return Singletonインスタンス
     */
    public static MonsterRepository getInstance(Application application, MonsterManager monsterManager ) {
        // 起動時はインスタンスを生成する
        synchronized (MonsterRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new MonsterRepository(application, monsterManager);
            }
            return INSTANCE;
        }
    }

    /**
     * モンスター情報をリセットする
     */
    public void reset() {
        updateMonster(new Monster(1, StateCode.NORMAL, "アグモン", 6, 6,3));
    }

    /**
     * モンスター情報をセットする
     * @param newMonster 新しいモンスター情報
     */
    public void updateMonster(@NonNull Monster newMonster) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // nullチェック（データベース接続前はnullの場合があるため）
                if (monsterDao == null) { return; }
                monsterDao.updateMonster(newMonster);
            } catch (Exception e) {
                Log.e("database event", "cannot update monster", e);
            }
            // 更新に成功したらManagerに通知
            monsterUpdateListener.onUpdatedMonster(newMonster);
        });
    }
}
