package com.example.monsterapp.model.entity.monster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.monsterapp.model.state.StateCode;

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
    @NonNull public String name;

    /** 状態 */
    @ColumnInfo(name = "monster_state")
    @NonNull public StateCode stateCode;

    /** 現在のHP */
    @ColumnInfo(name = "hp")
    public int hp;

    /** MaxのHP */
    @ColumnInfo(name = "max_hp")
    public int maxHp;

    /** 攻撃力 */
    @ColumnInfo(name = "power")
    public int power;

    /** コンストラクタ */
    public Monster(int uid, @NonNull StateCode stateCode, @NonNull String name, int hp, int maxHp, int power) {
        this.uid = uid;
        this.stateCode = stateCode;
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.power = power;
    }

    /**
     * モンスターの情報をコピーする
     * @return コピーしたモンスター情報
     */
    public Monster copy() {
        return new Monster(this.uid, this.stateCode, this.name, this.hp, this.maxHp, this.power);
    }
}
