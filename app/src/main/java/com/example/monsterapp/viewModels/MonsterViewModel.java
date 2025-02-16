package com.example.monsterapp.viewModels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import com.example.monsterapp.models.entity.monster.state.StateCode;
import com.example.monsterapp.models.manager.MonsterManager;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.MonsterViewData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * ViewModel
 */
public class MonsterViewModel extends AndroidViewModel{

    // LiveData
    /** モンスターの基本情報を管理するLiveData */
    @NonNull private final MutableLiveData<Monster> monsterLiveData = new MutableLiveData<>();
    /** モンスターの描画データを管理するLiveData */
    @NonNull private final MutableLiveData<MonsterViewData> monsterViewLiveData = new MutableLiveData<>();
    /** ボタンの活性状態を管理するLiveData */
    @NonNull private final MutableLiveData<Map<EventCode, Boolean>> buttonStatesLiveData =new MutableLiveData<>();
    /** loadingボタンを管理するLiveData */
    @NonNull private final MutableLiveData<Boolean> loadingModalLiveData = new MutableLiveData<>();

    /** Model */
    @Nullable private MonsterManager monsterManager = null;
    /** disposable */
    @NonNull private final CompositeDisposable disposables = new CompositeDisposable();
    /** モンスター描画データの管理マップ */
    @NonNull private Map<StateCode, MonsterViewData> monsterViewDataMap = new HashMap<>();

    /**
     * コンストラクタ
     * @param application　コンテキスト
     */
    @SuppressLint("CheckResult")
    public MonsterViewModel(Application application) {
        super(application);
        
        Map<EventCode, Boolean> btnStates = new HashMap<>();
        btnStates.put(EventCode.FEED, true);
        btnStates.put(EventCode.CURE, true);
        btnStates.put(EventCode.BLE_BATTLE, true);
        btnStates.put(EventCode.TOILET, true);
        btnStates.put(EventCode.RESET, true);
        buttonStatesLiveData.setValue(btnStates);

        loadingModalLiveData.setValue(false);

        // 描画データをロード
        loadViewData(application);

        monsterManager = new MonsterManager(application);
        disposables.add(
                monsterManager.getMonsterSubject()
                        .distinctUntilChanged() // 同じデータをフィルタリング
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::fetchMonsterData,
                                throwable -> Log.d("update Error", "monster cannot be updated")
                        )
        );
    }



    @Override
    protected void onCleared() {
        Log.d("LifeCycleEvent ViewModel", "View Model is cleared");
        super.onCleared();

        if (monsterManager != null) {
            monsterManager.clear();
        }
        // 購読を解除
        disposables.clear();
    }

    /**
     * モンスター情報を取得する
     * データベースの値が更新されるタイミングで画面に反映する
     * @return モンスター情報
     */
    @NonNull
    public LiveData<Monster> getMonsterLiveData() { return monsterLiveData; }

    /**
     * ボタンの活性状態を取得する
     * @return ボタンの活性状態
     */
    @NonNull
    public MutableLiveData<Map<EventCode, Boolean>> getButtonStatesLiveData() { return buttonStatesLiveData; }

    /**
     * モンスターの描画データを取得する
     * @return モンスターの描画データ
     */
    @NonNull
    public MutableLiveData<MonsterViewData> getMonsterViewLiveData() { return monsterViewLiveData; }

    /**
     * loadingモーダルの表示有無を取得する
     * @return loadingモーダルの表示有無
     */
    @NonNull
    public MutableLiveData<Boolean> getLoadingModalLiveData() {
        return loadingModalLiveData;
    }

    /**
     * ユーザのアクションイベント
     * @param event　イベント
     */
    public void onClickEvent(@NonNull Event event) {
        if (monsterManager == null) { return; }

        // 「対戦する」ボタン押下時、通信中モーダルを表示する
        // 他ボタンイベントを受け付けない
        if (event.eventCode == EventCode.BLE_BATTLE) {
            Log.d("battle event", String.valueOf(event.eventCode));
            loadingModalLiveData.setValue(true);

            Map<EventCode, Boolean> buttonStatesMap = buttonStatesLiveData.getValue();
            if(buttonStatesMap == null) { return; }
            for (Map.Entry<EventCode, Boolean> entry: buttonStatesMap.entrySet()) {
                entry.setValue(false);
            }
            buttonStatesLiveData.postValue(buttonStatesMap);
        }
        monsterManager.handleEvent(event);
    }

    /**
     * Modelのデータと同期をとる
     * @param newMonster 新しいモンスター情報
     */
    private void fetchMonsterData(@Nullable Monster newMonster) {
        if (newMonster == null) { return; }
        if (buttonStatesLiveData.getValue() == null) { return; }

        // 描画データを更新する
        monsterViewLiveData.postValue(monsterViewDataMap.get(newMonster.stateCode));

        // ボタンの活性状態を更新する
        Map<EventCode, Boolean> buttonStatesMap = buttonStatesLiveData.getValue();
        for (Map.Entry<EventCode, Boolean> entry: buttonStatesMap.entrySet()) {
            if (newMonster.stateCode == StateCode.DEATH) {
                entry.setValue(entry.getKey() == EventCode.RESET);
            }
            else {
                entry.setValue(true);
            }
        }
        buttonStatesLiveData.postValue(buttonStatesMap);

        // モンスター情報を更新する
        monsterLiveData.postValue(newMonster);
    }

    /**
     * jsonファイルか描画データを取得する
     * @param application コンテキスト
     */
    private void loadViewData(Application application) {
        //TODO 進化機能実装後、filepath修正予定
        String filePath = "viewData.json";

        // 描画データの取得
        try {
            String jsonString = getString(application, filePath);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<StateCode, MonsterViewData>>() {}.getType();
            monsterViewDataMap =  gson.fromJson(jsonString, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * jsonファイルをString型で受け取る
     * @param application アプリケーション
     * @param filePath ファイルパス
     * @return String型に変換されたjsonデータ
     * @throws IOException 例外
     */
    @NonNull
    private static String getString(Application application, String filePath) throws IOException {
        AssetManager assetManager = application.getAssets();
        InputStream inputStream = assetManager.open(filePath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }
}
