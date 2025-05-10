package com.example.monsterapp.ui.viewModel;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import com.example.monsterapp.model.data.repository.MonsterRepository;
import com.example.monsterapp.model.entity.battle.BattleStatus;
import com.example.monsterapp.model.state.State;
import com.example.monsterapp.usecase.BattleUseCase;
import com.example.monsterapp.usecase.MonsterStateUseCase;
import com.example.monsterapp.ui.data.buttonState;
import com.example.monsterapp.model.state.StateCode;
import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.model.entity.monster.Monster;
import com.example.monsterapp.ui.data.MonsterViewData;
import com.example.monsterapp.util.Event.EventCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel
 */
@HiltViewModel
public class MonsterViewModel extends AndroidViewModel{

    // LiveData
    /** モンスターの基本情報を管理するLiveData */
    @NonNull private final MutableLiveData<Monster> monsterLiveData = new MutableLiveData<>();
    /** モンスターの描画データを管理するLiveData */
    @NonNull private final MutableLiveData<MonsterViewData> monsterViewLiveData = new MutableLiveData<>();
    /** ボタンの活性状態を管理するLiveData */
    @NonNull private final MutableLiveData<buttonState> buttonStatesLiveData = new MutableLiveData<>();

    /** Model */
    @NonNull private final MonsterRepository repository;
    @NonNull private final MonsterStateUseCase stateUseCase;
    @NonNull private final BattleUseCase battleUseCase;
    /** disposable */
    @NonNull private final CompositeDisposable disposables = new CompositeDisposable();
    /** モンスター描画データの管理マップ */
    @NonNull private Map<StateCode, MonsterViewData> monsterViewDataMap = new HashMap<>();

    /**
     * コンストラクタ（Hilt用）
     * @param application アプリケーション
     * @param repository モンスターリポジトリ
     * @param stateUseCase モンスター状態ユースケース
     * @param battleUseCase 対戦ユースケース
     */
    @Inject
    public MonsterViewModel(
            Application application,
            MonsterRepository repository,
            MonsterStateUseCase stateUseCase,
            BattleUseCase battleUseCase) {
        super(application);

        this.repository = repository;
        this.stateUseCase = stateUseCase;
        this.battleUseCase = battleUseCase;

        // 通常時のボタンを表示する
        buttonState buttonState = new buttonState();
        buttonState.setState(1);
        buttonStatesLiveData.setValue(buttonState);

        // 描画データをロード
        loadViewData(application);

        // モンスターの監視
        disposables.add(
                repository.getMonster(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                monster -> {
                                    monsterLiveData.setValue(monster);
                                    Log.d("MonsterViewModel", "Monster updated: " + monster.name);
                                },
                                throwable -> Log.e("MonsterViewModel", "Error updating monster", throwable)
                        )
        );

        // 状態の監視
        disposables.add(
                stateUseCase.observeCurrentState()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                newState -> {
                                    updateMonsterView(newState);
                                    updateButtonStateByMonsterState(newState);
                                    Log.d("MonsterViewModel", "State updated: " + newState.stateCode);
                                },
                                throwable -> Log.e("MonsterViewModel", "Error observing state", throwable)
                        )
        );

        // 対戦状態の監視
        disposables.add(
                battleUseCase.observeBattleStatus()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                newBattleStatus -> {
                                    updateButtonStateByBattleStatus(newBattleStatus);
                                    Log.d("MonsterViewModel", "Battle status updated: " + newBattleStatus);
                                },
                                throwable -> Log.e("MonsterViewModel", "Error observing battle status", throwable)
                        )
        );
    }




    @Override
    protected void onCleared() {
        Log.d("LifeCycleEvent ViewModel", "View Model is cleared");
        super.onCleared();
        // 購読を解除
        disposables.clear();

        // UseCaseのクリーンアップ
        stateUseCase.cleanup();
        battleUseCase.cleanup();
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
    public MutableLiveData<buttonState> getButtonStatesLiveData() { return buttonStatesLiveData; }

    /**
     * モンスターの描画データを取得する
     * @return モンスターの描画データ
     */
    @NonNull
    public MutableLiveData<MonsterViewData> getMonsterViewLiveData() { return monsterViewLiveData; }

    /**
     * 状態変化イベントクリック
     * @param event　イベント
     */
    public void onClickEvent(@NonNull Event event) {
        Log.d("MonsterViewModel", "onClickEvent" + event.eventCode.toString());
        Monster currentMonster = monsterLiveData.getValue();
        if (currentMonster == null) return;

        if (event.eventCode == EventCode.ESCAPE) {
            battleUseCase.onClickEscape();
        } else if (event.eventCode == EventCode.BLE_BATTLE_TRIGGERED) {
            battleUseCase.onClickBleBattle();
        } else {
            stateUseCase.handleEvent(event);
        }
    }

    /**
     * モンスターの状態を元に描画データを更新する
     * @param newState 新しいモンスターの状態
     */
    private void updateMonsterView(@NonNull State newState) {
        Log.d("MonsterViewModel", "update monster view");
        MonsterViewData newMonsterViewData = monsterViewDataMap.get(newState.stateCode);
        monsterViewLiveData.postValue(newMonsterViewData);

        // 後々消す
        Monster monster = monsterLiveData.getValue();
        if (monster == null) { return; }
        monster.stateCode = newState.stateCode;
        monsterLiveData.postValue(monster);

    }

    /**
     * モンスターの状態を元にボタンの表示制御を更新する
     * @param newState 新しいモンスターの状態
     */
    private void updateButtonStateByMonsterState(@NonNull State newState) {
        if (newState.stateCode == StateCode.DEATH) {
            buttonState newButtonState = new buttonState();
            newButtonState.setState(4);
            buttonStatesLiveData.postValue(newButtonState);
        }
    }


    /**
     * 対戦ステータスを元にボタンの表示制御を更新する
     * @param newBattleStatus 新しい対戦ステータス
     */
    private void updateButtonStateByBattleStatus(@NonNull BattleStatus newBattleStatus) {
        Monster currentMonster = monsterLiveData.getValue();
        if (currentMonster == null || currentMonster.stateCode == StateCode.DEATH) {
            return;
        }

        buttonState buttonState = new buttonState();
        Log.d("MonsterViewModel", "update button state by battle status");

        switch (newBattleStatus) {
            case NORMAL:
                buttonState.setState(1);
                break;
            case READY_NPC_BATTLE:
                buttonState.setState(2);
                break;
            case READY_BLE_BATTLE:
                buttonState.setState(3);
                break;
            case NPC_BATTLE_START:
                buttonState.setState(5);
            default:
                buttonState.setState(5);
        }
        buttonStatesLiveData.postValue(buttonState);
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
