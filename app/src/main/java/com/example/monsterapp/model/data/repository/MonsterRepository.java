package com.example.monsterapp.model.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.model.state.State;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.data.room.AppDatabase;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.model.data.dao.MonsterDao;

import io.reactivex.Completable;
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

    /** 変更を監視するためのSubject */
    @NonNull final private BehaviorSubject<Monster> monsterSubject = BehaviorSubject.create();

    /**
     * コンストラクタ
     * @param application　コンテキスト
     */
    private MonsterRepository(@NonNull Application application) {
        try {
            @NonNull AppDatabase db = AppDatabase.getDatabase(application);
            monsterDao = db.monsterDao();
            // 初期データの読み込みを非同期実行
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    if (monsterDao == null) { return; }
                    Monster monster = monsterDao.getByUid(1);
                    if (monster != null) {
                        monsterSubject.onNext(monster);
                    } else {
                        // 初期データがない場合はデフォルトを作成
                        Monster defaultMonster = createDefaultMonster();
                        monsterDao.updateMonster(defaultMonster);
                        monsterSubject.onNext(defaultMonster);
                    }
                } catch (Exception e) {
                    Log.e("MonsterRepository", "初期データの読み込みに失敗しました", e);
                }
            });
        } catch (Exception e) {
            Log.e("MonsterRepository", "データベースの初期化に失敗しました", e);
        }
    }

    private Monster createDefaultMonster() {
        return new Monster(1, StateCode.NORMAL, "アグモン", 6, 6, 3);
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
     * モンスター情報を取得
     * @param id モンスターのID
     * @return モンスター情報のObservable
     */
    @NonNull
    public BehaviorSubject<Monster> getMonster(int id) {
        return monsterSubject;
    }

    /**
     * モンスターを更新する
     * @param newMonster 新しいモンスター
     */
    public void updateMonster(@NonNull Monster newMonster) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // nullチェック（データベース接続前はnullの場合があるため）
                if (monsterDao == null) return;
                monsterDao.updateMonster(newMonster);

                // 成功したら監視元に通知
                monsterSubject.onNext(newMonster);
            } catch (Exception e) {
                Log.e("database event", "cannot update monster", e);
            }
        });
    }

    /**
     * モンスターのリセット
     */
    public void reset() {
        updateMonster(createDefaultMonster());
    }

}
