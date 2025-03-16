package com.example.monsterapp.ui.viewModel;

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

import com.example.monsterapp.model.manager.battle.BattleStatus;
import com.example.monsterapp.ui.button.UIButtonState;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.model.manager.MonsterManager;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.ui.model.MonsterViewData;
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
    @NonNull private final MutableLiveData<UIButtonState> buttonStatesLiveData = new MutableLiveData<>();
    /** バトルログ */
    @NonNull private final MutableLiveData<String> battleLogLiveData = new MutableLiveData<>();

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

        // 通常時のボタンを表示する
        UIButtonState uiButtonState = new UIButtonState();
        uiButtonState.setState(1);
        buttonStatesLiveData.setValue(uiButtonState);

        // 描画データをロード
        loadViewData(application);

        monsterManager = new MonsterManager(application);
        disposables.add(
                monsterManager.monsterSubject
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::fetchMonsterData,
                                throwable -> Log.d("update Error", "monster cannot be updated")
                        )
        );
        disposables.add(
                monsterManager.battleStatusSubject
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::onChangedBattleStatus,
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
    public MutableLiveData<UIButtonState> getButtonStatesLiveData() { return buttonStatesLiveData; }

    /**
     * モンスターの描画データを取得する
     * @return モンスターの描画データ
     */
    @NonNull
    public MutableLiveData<MonsterViewData> getMonsterViewLiveData() { return monsterViewLiveData; }

    /**
     * ユーザのアクションイベント
     * @param event　イベント
     */
    public void onClickEvent(@NonNull Event event) {
        if (monsterManager == null) { return; }
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

        UIButtonState newUiButtonState = new UIButtonState();
        // 死亡状態であれば、「リセット」ボタンのみ表示
        if (newMonster.stateCode == StateCode.DEATH) {
            newUiButtonState.setState(4);
            buttonStatesLiveData.postValue(newUiButtonState);
        }

        // モンスター情報を更新する
        monsterLiveData.postValue(newMonster);
    }

    /**
     * 対戦ステータスを基に画面のボタンを制御する
     * @param newBattleStatus 新しい対戦ステータス
     */
    private void onChangedBattleStatus(@NonNull BattleStatus newBattleStatus) {
        UIButtonState uiButtonState = new UIButtonState();
        Log.d("onChangedBattleStatus", "update ui state");

        switch (newBattleStatus) {
            case NORMAL:
                uiButtonState.setState(1);
                break;
            case READY_NPC_BATTLE:
                uiButtonState.setState(2);
                break;
            case READY_BLE_BATTLE:
                uiButtonState.setState(3);
                break;
            case NPC_BATTLE_START:
            case BLE_BATTLE_START:
            case ATTACKING:
            case ATTACKED:
            case WIN:
            case LOSE:
                Log.d("update ui", "battling");
                uiButtonState.setState(5);
                break;
            default:
                uiButtonState.setState(99);
        }
        buttonStatesLiveData.postValue(uiButtonState);
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
