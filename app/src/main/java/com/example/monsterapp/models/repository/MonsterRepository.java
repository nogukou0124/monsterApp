package com.example.monsterapp.models.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.room.AppDatabase;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.dao.MonsterDao;
import com.example.monsterapp.models.manager.state.MonsterStateMachine;

import java.util.Objects;

import io.reactivex.rxjava3.subjects.BehaviorSubject;


/**
 * モンスター情報を管理するクラス
 * ViewModelの橋渡しを行う
 */
public class MonsterRepository {
    /** Singletonインスタンス */
    @Nullable private static MonsterRepository INSTANCE = null;
    /** DAO */
    @Nullable MonsterDao monsterDao = null;
    /** モンスター情報 */
    @Nullable Monster currentMonster = null;

    /**
     * コンストラクタ
     * @param application　コンテキスト
     */
    private MonsterRepository(@NonNull Application application) {
        try {
            // データベース接続
            @NonNull AppDatabase db = AppDatabase.getDatabase(application);
            monsterDao = db.monsterDao();

            AppDatabase.databaseWriteExecutor.execute(() -> {
                currentMonster = Objects.requireNonNull(monsterDao,"monsterDao is null").getByUid(1);
                // データベースに情報が登録されていない場合は、デフォルトデータを挿入
                if (currentMonster == null) {
                    Log.e("database event", "No Monster found with uid = 1");
                    currentMonster = new Monster(1, StateCode.NORMAL, "アグモン", 6, 3);
                    monsterDao.insert(currentMonster);
                    return;
                }
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
    public static MonsterRepository getInstance(Application application) {
        // 起動時はインスタンスを生成する
        synchronized (MonsterRepository.class) {
            if (INSTANCE == null) {
                INSTANCE = new MonsterRepository(application);
            }
            return INSTANCE;
        }
    }

    /**
     * モンスター情報を取得する
     * @return モンスター
     */
    @NonNull
    public Monster getMonster() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentMonster = Objects.requireNonNull(monsterDao,"monsterDao is null").getByUid(1);
            // データベースに情報が登録されていない場合は、デフォルトデータを挿入
            if (currentMonster == null) {
                Log.e("database event", "No Monster found with uid = 1");
                currentMonster = new Monster(1, StateCode.NORMAL, "アグモン", 6, 3);
                monsterDao.insert(currentMonster);
                return;
            }
        });
        return currentMonster;
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
        });
    }
}
