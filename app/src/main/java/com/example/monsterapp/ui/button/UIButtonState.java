package com.example.monsterapp.ui.button;

public class UIButtonState {
    public boolean isFeedBtnShown;
    public boolean isToiletBtnShown;
    public boolean isCureBtnShown;
    public boolean isBattleBtnShown;
    public boolean isResetBtnShown;
    public boolean isEscapeBtnShown;
    public boolean isLoadingShown;

    public UIButtonState() {}

    /**
     * モードに従ってボタンの状態を更新する<br>
     * 1 : 通常時<br>
     * 2 : NPC対戦発生時<br>
     * 3 : BLE対戦開始時<br>
     * 4 : 死亡時<br>
     * 5 : 対戦中<br>
     * その他 : 不正な状態<br>
     * @param mode モード
     */
    public void setState(int mode) {
        switch (mode) {
            case 1:
                isFeedBtnShown = true;
                isToiletBtnShown = true;
                isCureBtnShown = true;
                isBattleBtnShown = true;
                isResetBtnShown = true;
                isEscapeBtnShown = false;
                isLoadingShown = false;
                break;
            case 2:
                isFeedBtnShown = false;
                isToiletBtnShown = false;
                isCureBtnShown = false;
                isBattleBtnShown = false;
                isResetBtnShown = false;
                isEscapeBtnShown = true;
                isLoadingShown = false;
                break;
            case 3:
                isFeedBtnShown = false;
                isToiletBtnShown = false;
                isCureBtnShown = false;
                isBattleBtnShown = false;
                isResetBtnShown = false;
                isEscapeBtnShown = false;
                isLoadingShown = true;
                break;
            case 4:
                isFeedBtnShown = false;
                isToiletBtnShown = false;
                isCureBtnShown = false;
                isBattleBtnShown = false;
                isResetBtnShown = true;
                isEscapeBtnShown = false;
                isLoadingShown = false;
                break;
            case 5:
                isFeedBtnShown = false;
                isToiletBtnShown = false;
                isCureBtnShown = false;
                isBattleBtnShown = false;
                isResetBtnShown = false;
                isEscapeBtnShown = false;
                isLoadingShown = false;
                break;
            default:
                isFeedBtnShown = false;
                isToiletBtnShown = false;
                isCureBtnShown = false;
                isBattleBtnShown = false;
                isResetBtnShown = false;
                isEscapeBtnShown = false;
                isLoadingShown = false;
                break;
        }
    }
}
