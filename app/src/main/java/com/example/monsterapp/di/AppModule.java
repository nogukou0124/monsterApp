package com.example.monsterapp.di;

import android.app.Application;

import com.example.monsterapp.model.data.repository.MonsterRepository;
import com.example.monsterapp.usecase.BattleUseCase;
import com.example.monsterapp.usecase.MonsterStateUseCase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public MonsterRepository provideMonsterRepository(Application application) {
        return MonsterRepository.getInstance(application);
    }

    @Provides
    @Singleton
    public MonsterStateUseCase provideMonsterStateUseCase(MonsterRepository repository) {
        return new MonsterStateUseCase(repository);
    }

    @Provides
    @Singleton
    public BattleUseCase provideBattleUseCase(MonsterRepository repository, MonsterStateUseCase monsterStateUseCase) {
        return new BattleUseCase(repository, monsterStateUseCase);
    }
}
