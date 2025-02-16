package com.example.monsterapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.monsterapp.utils.Event.Event;
import com.example.monsterapp.utils.Event.EventCode;
import com.example.monsterapp.R;
import com.example.monsterapp.viewModels.MonsterViewModel;

/**
 * 画面全体の描画を行うフラグメント
 */
public class MonsterViewFragment extends Fragment {

    /** ViewModel */
    @Nullable MonsterViewModel monsterViewModel = null;

    /**
     * コンストラクタ
     */
    private MonsterViewFragment() {}

    /**
     * インスタンス生成関数
     * @return インスタンス
     */
    @NonNull
    public static MonsterViewFragment newInstance() {
        return new MonsterViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monster_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModelの初期化
        monsterViewModel = new ViewModelProvider(requireActivity()).get(MonsterViewModel.class);

        // モンスター情報の監視
        // モンスター描画データの監視
        @NonNull MonsterView monsterView = view.findViewById(R.id.monster_view);

        monsterViewModel.getMonsterViewLiveData().observe(getViewLifecycleOwner(), newMonsterViewData -> {
            if (newMonsterViewData == null) { return; }
            Log.d("update event","monster view data is updated");
            monsterView.setMonsterViewData(newMonsterViewData);
        });

        // モンスター情報の監視
        @NonNull TextView textMonsterName = view.findViewById(R.id.monster_name);
        @NonNull TextView textMonsterHp = view.findViewById(R.id.monster_hp);
        @NonNull TextView textView = view.findViewById(R.id.monster_power);

        monsterViewModel.getMonsterLiveData().observe(getViewLifecycleOwner(), newMonster -> {
            if (newMonster == null) { return; };
            Log.d("update event","monster is updated");
            textMonsterName.setText(String.format("状態: %s", newMonster.stateCode));
            textMonsterHp.setText(String.format("HP: %s", newMonster.hp));
            textView.setText(String.format("攻撃力: %s", newMonster.power));
        });

        // アクションボタン活性状態の監視
        @NonNull Button feedButton = view.findViewById(R.id.feed_btn);
        feedButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.FEED)));
        @NonNull Button toiletButton = view.findViewById(R.id.toilet_btn);
        toiletButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.TOILET)));
        @NonNull Button cureButton = view.findViewById(R.id.cure_btn);
        cureButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.CURE)));
        @NonNull Button battleButton = view.findViewById(R.id.battle_btn);
        battleButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.BLE_BATTLE)));
        @NonNull Button resetButton = view.findViewById(R.id.reset_btn);
        resetButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.RESET)));

        monsterViewModel.getButtonStatesLiveData().observe(getViewLifecycleOwner(), newButtonStates -> {
            if (newButtonStates == null) { return; }
            Log.d("update event","button states are updated");
            feedButton.setVisibility(Boolean.TRUE.equals(newButtonStates.get(EventCode.FEED)) ? View.VISIBLE : View.INVISIBLE);
            toiletButton.setVisibility(Boolean.TRUE.equals(newButtonStates.get(EventCode.TOILET)) ? View.VISIBLE : View.INVISIBLE);
            cureButton.setVisibility(Boolean.TRUE.equals(newButtonStates.get(EventCode.CURE)) ? View.VISIBLE : View.INVISIBLE);
            battleButton.setVisibility(Boolean.TRUE.equals(newButtonStates.get(EventCode.BLE_BATTLE)) ? View.VISIBLE : View.INVISIBLE);
            resetButton.setVisibility(Boolean.TRUE.equals(newButtonStates.get(EventCode.RESET)) ? View.VISIBLE : View.INVISIBLE);
        });

        // loadingモーダルの表示状態の監視
        @NonNull ProgressBar loadingModal = view.findViewById(R.id.loading);
        monsterViewModel.getLoadingModalLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            loadingModal.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        });
    }
}