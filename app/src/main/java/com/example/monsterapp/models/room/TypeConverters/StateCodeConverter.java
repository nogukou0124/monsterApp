package com.example.monsterapp.models.room.TypeConverters;

import androidx.room.TypeConverter;

import com.example.monsterapp.models.entity.monster.state.StateCode;


public class StateCodeConverter {
    @TypeConverter
    public static String fromState(StateCode stateCode) {
        return stateCode == null ? "1" : String.valueOf(stateCode);
    }

    @TypeConverter
    public static StateCode toState(String strState) {
        return strState == null ? StateCode.NORMAL : StateCode.valueOf(strState);
    }
}
