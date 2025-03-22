package com.example.monsterapp.util.callback;

import com.example.monsterapp.model.state.State;
import com.example.monsterapp.util.Event.Event;

public interface StateEventListener {
    public abstract void onUpdatedState(State oldState, State newState);
}
