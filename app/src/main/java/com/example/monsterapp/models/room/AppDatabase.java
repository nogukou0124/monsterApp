package com.example.monsterapp.models.room;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.dao.MonsterDao;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.models.room.TypeConverters.StateCodeConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Monster.class}, version = 3, exportSchema = false)
@TypeConverters({StateCodeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MonsterDao monsterDao();
    private static volatile AppDatabase INSTANCE = null;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                try {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "monster_database")
                            .fallbackToDestructiveMigration()
//                            .addCallback(sRoomDatabaseCallback)
                            .build();
                    Log.d("AppDatabase", "Database instance created successfully.");
                } catch (Exception e) {
                    Log.e("AppDatabase", "Error creating database instance", e);
                }
            }
        }
        if (INSTANCE == null) {
            Log.e("AppDatabase", "Database instance is null after initialization.");
        }
        return INSTANCE;
    }

    /**
     * データベース生成時、初期状態を挿入する
     */
    private static final Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("AppDatabase", "Database created: " + db.getPath());
            databaseWriteExecutor.execute(() -> {
                try {
                    MonsterDao monsterDao = INSTANCE.monsterDao();
                    Monster monster = new Monster(1, StateCode.NORMAL, "アグモン", 6, 3);
                    monsterDao.insert(monster);
                    Log.d("AppDatabase", "Default data inserted successfully.");
                } catch (Exception e) {
                    Log.e("AppDatabase", "Error inserting default data", e);
                }
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d("AppDatabase", "onOpen called. Database path: " + db.getPath());
            databaseWriteExecutor.execute(() -> {
                try {
                    AppDatabase database = INSTANCE;
                    if (database != null) {
                        MonsterDao dao = database.monsterDao();
                        // 必要に応じてデフォルトデータを確認して挿入
                        if (dao.getByUid(1) == null) {
                            dao.insert(new Monster(1, StateCode.NORMAL, "アグモン", 6, 3));
                            Log.d("AppDatabase", "Default data inserted in onOpen.");
                        }
                    }
                } catch (Exception e) {
                    Log.e("AppDatabase", "Error inserting default data in onOpen", e);
                }
            });
        }
    };
}
