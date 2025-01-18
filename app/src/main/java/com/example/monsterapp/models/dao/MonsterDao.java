package com.example.monsterapp.models.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.monsterapp.models.entity.monster.Monster;

@Dao
public interface MonsterDao {

    @Query("SELECT * FROM monster WHERE uid = :monsterId")
    Monster getByUid(int monsterId);

    @Insert
    void insert(Monster monster);

    @Update
    public void updateMonster(Monster monster);

    @Delete
    public void deleteMonster(Monster monster);
}
