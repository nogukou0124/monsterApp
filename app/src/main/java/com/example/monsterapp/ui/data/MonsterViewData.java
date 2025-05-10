package com.example.monsterapp.ui.data;

/**
 * モンスター描画データクラス
 */
public class MonsterViewData {
    public float[][] viewData;
    public boolean isAnimation;

    public MonsterViewData(float[][] viewData, boolean isAnimation) {
        this.viewData = viewData;
        this.isAnimation = isAnimation;
    }
}
