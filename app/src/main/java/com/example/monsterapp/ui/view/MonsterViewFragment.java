package com.example.monsterapp.ui.view;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.monsterapp.util.Event.Event;
import com.example.monsterapp.util.Event.EventCode;
import com.example.monsterapp.R;
import com.example.monsterapp.ui.viewModel.MonsterViewModel;

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
        monsterViewModel = new ViewModelProvider(this).get(MonsterViewModel.class);

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
        @NonNull ProgressBar hpBar = view.findViewById(R.id.monster_hp_bar);
//        @NonNull TextView textView = view.findViewById(R.id.monster_power);

        monsterViewModel.getMonsterLiveData().observe(getViewLifecycleOwner(), newMonster -> {
            if (newMonster == null) { return; };
            Log.d("update event","monster is updated");
            textMonsterName.setText(String.format("状態: %s", newMonster.stateCode));
            textMonsterHp.setText(String.format("HP : %s / %s", newMonster.hp, newMonster.maxHp));
            hpBar.setMax(newMonster.maxHp);
            hpBar.setProgress(newMonster.hp);

            // 残りのHPによって色を変化させる
            int hpPercent = (int) (((float)newMonster.hp / newMonster.maxHp) * 100);
            if (hpPercent > 50) {
                hpBar.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.hp_green)));
            } else if (hpPercent > 25) {
                hpBar.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.hp_yellow)));
            } else {
                hpBar.setProgressTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.hp_red)));
            }
        });

        // アクションボタン活性状態の監視
        @NonNull Button feedButton = view.findViewById(R.id.feed_btn);
        feedButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.FEED)));
        @NonNull Button toiletButton = view.findViewById(R.id.toilet_btn);
        toiletButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.TOILET)));
        @NonNull Button cureButton = view.findViewById(R.id.cure_btn);
        cureButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.CURE)));
        @NonNull Button battleButton = view.findViewById(R.id.battle_btn);
        battleButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.BLE_BATTLE_TRIGGERED)));
        @NonNull Button resetButton = view.findViewById(R.id.reset_btn);
        resetButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.RESET)));
        @NonNull Button escapeButton = view.findViewById(R.id.escape_btn);
        escapeButton.setOnClickListener(v -> monsterViewModel.onClickEvent(new Event(EventCode.ESCAPE)));
        @NonNull ProgressBar loadingBar = view.findViewById(R.id.loading);

        monsterViewModel.getButtonStatesLiveData().observe(getViewLifecycleOwner(), newButtonStates -> {
            if (newButtonStates == null) { return; }
            Log.d("update event","button states are updated");
            feedButton.setVisibility(newButtonStates.isFeedBtnShown ? View.VISIBLE : View.GONE);
            toiletButton.setVisibility(newButtonStates.isToiletBtnShown ? View.VISIBLE : View.GONE);
            cureButton.setVisibility(newButtonStates.isCureBtnShown ? View.VISIBLE : View.GONE);
            battleButton.setVisibility(newButtonStates.isBattleBtnShown ? View.VISIBLE : View.GONE);
            resetButton.setVisibility(newButtonStates.isResetBtnShown ? View.VISIBLE : View.GONE);
            escapeButton.setVisibility(newButtonStates.isEscapeBtnShown ? View.VISIBLE : View.GONE);
            loadingBar.setVisibility(newButtonStates.isLoadingShown ? View.VISIBLE : View.GONE);
        });
    }
}