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

import com.example.monsterapp.models.manager.battle.BattleManager;
import com.example.monsterapp.models.manager.state.MonsterStateMachine;
import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.models.entity.monster.Monster;
import com.example.monsterapp.models.entity.MonsterViewData;
import com.example.monsterapp.models.entity.state.StateCode;
import com.example.monsterapp.models.repository.MonsterRepository;
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

    // Models
    /** モンスター情報を管理 */
    @Nullable private MonsterRepository repository = null;
    /** 状態遷移を管理 */
    @Nullable private MonsterStateMachine monsterStateMachine = null;

    /** disposable */
    @NonNull private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * モンスター描画データの管理マップ
     * モンスターの状態毎に描画データを管理する
     */
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
        btnStates.put(EventCode.BATTLE, true);
        btnStates.put(EventCode.TOILET, true);
        btnStates.put(EventCode.RESET, true);
        buttonStatesLiveData.setValue(btnStates);

        // 描画データをロード
        loadViewData(application);

        // モンスター情報が更新された際に通知を受け取る
        repository = MonsterRepository.getInstance(application);
        disposables.add(
                repository.getMonsterBehaviorSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        newMonster -> {
                            if (monsterStateMachine == null) {
                                initMonsterStateMachine();
                                monsterStateMachine.start(newMonster.stateCode);
                            }
                            fetchMonsterData(newMonster);
                        },
                        throwable -> Log.d("callback event","monster is updated")
                )
        );


    }

    /**
     * ステートマシンの初期化処理
     */
    private void initMonsterStateMachine() {
        // 状態管理の初期化
        monsterStateMachine = new MonsterStateMachine();
        // 状態が更新された際に通知を受け取る
        disposables.add(
                monsterStateMachine.getStateBehaviorSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                newState -> {
                                    Monster monster = monsterLiveData.getValue();
                                    if(monster == null || repository == null) { return; }
                                    monster.stateCode = newState.stateCode;
                                    repository.updateMonster(monster);
                                    fetchMonsterData(monster);
                                },
                                throwable -> Log.e("get state error", "cannot monster state type", throwable)
                        )
        );
    }

    /**
     * 対戦管理クラスの初期化処理
     * @param battleManager 対戦管理クラス
     */
    private void initBattleManager(BattleManager battleManager) {
        disposables.add(
                battleManager.getMonsterBehaviorSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                newBattleData -> {
                                    if(monsterStateMachine == null || repository == null) { return; }
                                    if(newBattleData.event == null) { return; }
                                    monsterStateMachine.handleEvent(newBattleData.event);
                                    repository.updateMonster(newBattleData.monster);
                                    fetchMonsterData(newBattleData.monster);
                                },
                                throwable -> Log.d("callback event","monster is updated")
                        )
        );
    }

    @Override
    protected void onCleared() {
        Log.d("LifeCycleEvent ViewModel", "View Model is cleared");
        super.onCleared();
        if (repository != null) {
            repository.clear();
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
     * ユーザのアクションイベント
     * @param event　イベント
     */
    public void onClickEvent(@NonNull Event event) {
        if (repository == null || monsterStateMachine == null) { return; }
        Log.d("onClick event", String.valueOf(event.eventCode));

        if (event.eventCode == EventCode.BATTLE) {
            //TODO BLE通信開始
            Log.d("battle event", String.valueOf(event.eventCode));
        }
        else {
            monsterStateMachine.handleEvent(event);
        }
    }

    /**
     * モンスター情報を更新する
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
