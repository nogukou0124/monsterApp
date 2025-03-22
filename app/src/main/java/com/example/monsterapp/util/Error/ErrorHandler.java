package com.example.monsterapp.util.Error;

import android.util.Log;

public class ErrorHandler {
    public static void handleDatabaseError(Exception e, String operation) {
        Log.e("Database", String.format("データベース操作 '%s' 中にエラーが発生しました", operation), e);
    }

    public static void handleStateError(Exception e, String operation) {
        Log.e("State", String.format("状態遷移 '%s' 中にエラーが発生しました", operation), e);
    }

    public static void handleBattleError(Exception e, String operation) {
        Log.e("Battle", String.format("バトル処理 '%s' 中にエラーが発生しました", operation), e);
    }

    public static void handleGeneralError(Exception e, String operation) {
        Log.e("General", String.format("処理 '%s' 中に予期せぬエラーが発生しました", operation), e);
    }
}
