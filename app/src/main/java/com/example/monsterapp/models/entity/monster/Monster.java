package com.example.monsterapp.models.entity.monster;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.monsterapp.models.entity.monster.state.StateCode;

/**
 * モンスタークラス
 */
@Entity
public class Monster {
    /** ID */
    @PrimaryKey
    public int uid;

    /** 名前　*/
    @ColumnInfo(name = "monster_name")
    public String name;

    /** 状態 */
    @ColumnInfo(name = "monster_state")
    public StateCode stateCode;

    /** HP */
    @ColumnInfo(name = "hp")
    public int hp;

    /** 攻撃力 */
    @ColumnInfo(name = "power")
    public int power;

    /** コンストラクタ */
    public Monster(int uid, StateCode stateCode, String name, int hp, int power) {
        this.uid = uid;
        this.stateCode = stateCode;
        this.name = name;
        this.hp = hp;
        this.power = power;
    }
}
